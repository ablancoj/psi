from functools import reduce
import operator
import time
import random


class EncryptedInteger(object):

    def __init__(self,value,pk,encrypted=False):
        self.pk = pk
        self.n = pk['n']
        self.N = pk['n'] ** 2
        self.g = pk['g']
        if encrypted:
            self.value = value
        else:
            self.r = ZZ.random_element(self.n)
            self.value = (power_mod(self.g,value,self.N) * power_mod(self.r,self.n,self.N)) % self.N

    def __repr__(self):
        return str(self.value)

    def __str__(self):
        return str(self.value)

    def __add__(self,b):
        if isinstance(b,EncryptedInteger):
            return EncryptedInteger((self.value * b.value) % self.N, self.pk, True)
        else:
            return EncryptedInteger((self.value * power_mod(self.g, b, self.N)) % self.N, self.pk, True)

    def __radd__(self,b):
        if isinstance(b,EncryptedInteger):
            return EncryptedInteger((self.value * b.value) % self.N, self.pk, True)
        else:
            return EncryptedInteger((self.value * power_mod(self.g, b, self.N)) % self.N, self.pk, True)

    def __mul__(self,b):
        return EncryptedInteger(power_mod(self.value,b,self.N), self.pk, True)

    def __rmul__(self,b):
        return EncryptedInteger(power_mod(self.value,b,self.N), self.pk, True)

    def __imul__(self,b):
        return NotImplemented

    def __eq__(self,b):
        return isinstance(b,EncryptedInteger) and self.value == b.value and self.pk == b.pk

    def rerandomize(self):
        return self + EncryptedInteger(0, self.pk, False)


    def decrypt(self, sk):
        l = sk['lambda']
        mu = sk['mu']
        return (((power_mod(self.value,l,self.N)-1)//self.n) * mu) % self.n

    def clearR(self):
        self.r = 0

class EncryptedPolynomial(object):

    def __init__(self, coefficients, pk):
        self.pk = pk
        if isinstance(coefficients[0],EncryptedInteger):
            self.coefficients = coefficients 
        else:
            self.coefficients = map(lambda x:EncryptedInteger(x,pk),coefficients)


    def evaluate(self,x):
        #result = EncryptedInteger(0, self.pk, True)
        result = self.coefficients[-1]
        for i in reversed(self.coefficients[:-1]):
            result = i + result * x
        return result

    def horner(self, x):
        result = self.coefficients[-1]
        for coefficient in reversed(self.coefficients[:-1]):
            result = coefficient + (result * x)
        return result

    def __imul__(self,x):
        self.coefficients = [c*x for c in self.coefficients]
        return self

    def rerandomize(self):
        return EncryptedPolynomial([x.rerandomize().value for x in self.coefficients],self.pk,True)

    def __repr__(self):
        return str(self.coefficients)

    def __str__(self):
        return ''.join(map(str,self.coefficients))
 
def coefficientsFromParameters(parameters):
    P.<x> = ZZ['x']
    return reduce(operator.mul, [(x-i) for i in parameters]).list()

def encryptedPolynomialFromParameters(parameters,pk):
    P.<x> = ZZ['x']
    return EncryptedPolynomial(coefficientsFromParameters(parameters),pk)

def KeyGen(sec=1024):
    sec /= 2
    p = q = 0;
    while p == q or gcd(p*q, (p-1)*(q-1)) != 1:
        p = random_prime(2**(sec+1)-1, lbound = 2**sec)
        q = random_prime(2**(sec+1)-1, lbound = 2**sec)
    n = p * q
    #g = int(Integers(n**2).random_element())
    g = n + 1
    #l = lcm((p-1),(q-1))
    l = ((p-1)*(q-1))
    #mu = inverse_mod((power_mod(g,l,n**2)-1)//n,n)
    mu = inverse_mod(l,n)
    return {'n':n, 'g':g}, {'lambda':l, 'mu':mu, 'p':p, 'q':q}

def BuildPrivateSet(userSet):
    pk,sk=KeyGen()
    privateSet = encryptedPolynomialFromParameters(userSet,pk)
    return privateSet,pk,sk

def IsInSet(privateSet,x,sk):
    return privateSet.evaluate(x).decrypt(sk) == 0

def TestSet(privateSet,userSet,pk,shared):
    result = []
    encShared = EncryptedInteger(shared,pk)
    for i in userSet:
        r = ZZ.random_element(pk['n'])
        result.append(privateSet.evaluate(i) * r + encShared)
    return result

def GetCardinality(test, sk, shared):
    result = 0
    for i in test:
        if i.decrypt(sk) == shared:
            result += 1
    return result

def TestTimes():
    userSet = [random.randint(2**64,(2**65)-1) for _ in range(50)]
    sample = [random.randint(2**64,(2**65)-1) for _ in range(50)]
    start = time.clock()
    privateSet, pk, sk = BuildPrivateSet(userSet)
    test = TestSet(privateSet, sample, pk, 01010101010101)
    GetCardinality(test, sk, 01010101010101)
    print time.clock()-start

def PaillierTest():
    pk, sk = KeyGen()
    m1, m2, m3 = 5, 7, 8
    prefs = [m1, m2, m3]
    c1 = EncryptedInteger(m1,pk)
    c2 = EncryptedInteger(m2,pk)
    p = encryptedPolynomialFromParameters(prefs,pk)
    r = TestSet(p,[2,4,5],pk,01010101010)

    print 'Testing cipher.'
    print 'm1 == Dec(Enc(m1,pk),sk)...',
    assert m1 == c1.decrypt(sk), 'Encryption/Decryption failed.'
    print 'OK'
    print 'm2 == Dec(Enc(m2,pk),sk)...',
    assert m2 == c2.decrypt(sk), 'Encryption/Decryption failed.'
    print 'OK'
    print 'm1 != Dec(Enc(m2,pk),sk)...',
    assert m1 != c2.decrypt(sk), 'Encryption/Decryption failed.'
    print 'OK'
    print '1 == Dec(Enc(1,pk),sk)...',
    assert 1 == EncryptedInteger(1,pk).decrypt(sk), 'Encryption/Decryption failed.'
    print 'OK'
    print '0 == Dec(Enc(0,pk),sk)...',
    assert 0 == EncryptedInteger(0,pk).decrypt(sk), 'Encryption/Decryption failed.'
    print 'OK'
    print '0 == Dec(Enc(n,pk),sk)...',
    assert 0 ==  EncryptedInteger(pk['n'],pk).decrypt(sk), 'Encryption/Decryption failed.'
    print 'OK'

    print 'Testing homomorphic properties.'
    print 'Enc(m1+m2) == Enc(m1)+Enc(m2)...',
    assert m1+m2 == (c1+c2).decrypt(sk), 'Sum of ciphertexts failed.' 
    print 'OK'
    print 'Commutativity of the sum...',
    assert m1+m2 == (c2+c1).decrypt(sk), 'Sum of ciphertexts failed.'
    print 'OK'
    print 'Enc(m1+m2) == Enc(m1)+m2...',
    assert m1+m2 == (c1+m2).decrypt(sk), 'Sum of ciphertext and plaintext failed.'
    print 'OK'
    print 'Commutativity of the sum...',
    assert m1+m2 == (m2+c1).decrypt(sk), 'Sum of ciphertext and plaintext failed.'
    print 'OK'
    print 'Enc(m1*m3) == Enc(m1)*m3...',
    assert m1*m3 == (c1*m3).decrypt(sk), 'Product of ciphertext and plaintext failed.'
    print 'OK'
    print 'Commutativity of the product...',
    assert m1*m3 == (m3*c1).decrypt(sk), 'Product of ciphertext and plaintext failed.'
    print 'OK'
    print 'Distributive property of sum and product...',
    assert m3*m1 + m3*m2 == (m3*(c1+c2)).decrypt(sk), 'Homomorphic properties failed.'
    assert m3*m1 + m3*m2 == ((c2+c1)*m3).decrypt(sk), 'Homomorphic properties failed.'
    print 'OK'
    print 'Testing private matching.'
    print 'm1 in Enc({m1,m2,m3})...', 
    assert IsInSet(p, m1, sk), 'IsInSet failed.'
    print 'OK'
    print 'm2 in Enc({m1,m2,m3})...',
    assert IsInSet(p, m2, sk), 'IsInSet failed.'
    print 'OK'
    print 'm4 not in Enc({m1,m2,m3})...',
    assert ~IsInSet(p, 6, sk), 'IsInSet failed.'
    print 'OK'
    print '|{2,4,5} n Enc({5,7,8})| == 1...',
    assert 1 == GetCardinality(r,sk,01010101010), 'Cardinality failed.'
    print 'OK'
    print 'All tests finished successfully.'
