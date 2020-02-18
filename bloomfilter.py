import hmac
from base64 import urlsafe_b64encode
from hashlib import sha512
from math import floor, log

from bitarray import bitarray

def expand(func):
	return [bytes('({}-{})'.format(i+1,c),encoding='ascii') for i,x in enumerate(func) for c in range(1,x+1)]

def comparesets(a, b, m, k, pin, repeats=0):
	bfa = BloomFilter(m,k,pin)
	bfb = BloomFilter(m,k,pin)
	bfa.addall(a)
	bfb.addall(b)
	return bfa.jaccard_distance(bfb)

def comparefunctions(f, g, m, k, pin, repeats=0):
	bff = BloomFilter(m,k,pin)
	bfg = BloomFilter(m,k,pin)
	bff.addall(expand(f))
	bfg.addall(expand(g))
	for i in range(repeats):
		result = 2*bff.union_encoded_estimate(bfg) - bff.encoded_estimate() - bfg.encoded_estimate()
	return result 

class BloomFilter(object):
	def __init__(self, m=1024, k=1, pin=b'0'):
		self.m = m
		self.k = k
		self.pin = pin
		self.slice = self.m // self.k
		self.a = bitarray(m)
		self.a.setall(False)
		self.h = lambda x: int.from_bytes(sha512(x).digest(), byteorder='big')
		self.g = lambda x: int.from_bytes(hmac.new(pin, x, sha512).digest(), byteorder='big')
	
	def __getitem__(self, i):

		return self.a[i]

	def __setitem__(self, i, v):
		self.a[i] = v

	def __str__(self):
		return self.a.to01()

	def __and__(self, other):
		if self.m != other.m or self.k != other.k:
			raise Exception('Operation error', 'Length of bloom filters does not match.')
		c = BloomFilter(self.m, self.k, self.pin)
		#for i in range(self.m):
		#	c.a[i] = self.a[i] & other.a[i]
		c.a = self.a & other.a
		return c

	def __or__(self, other):
		if self.m != other.m or self.k != other.k:
			raise Exception('Operation error', 'Length of bloom filters does not match.')
		c = BloomFilter(self.m, self.k, self.pin)
		#for i in range(self.m):
		#	c.a[i] = self.a[i] | other.a[i]
		c.a = self.a | other.a
		return c

	def __xor__(self, other):
		if self.m != other.m or self.k != other.k:
			raise Exception('Operation error', 'Length of bloom filters does not match.')
		c = BloomFilter(self.m, self.k, self.pin)
		#for i in range(self.m):
		#	c.a[i] = self.a[i] ^ other.a[i]
		c.a = self.a ^ other.a
		return c

	def __contains__(self, item):
		base = self.h(item)
		added = self.g(item)
		for i in range(1, self.k + 1):
			index = ((base + i * added) % self.slice) + (i - 1) * self.slice
			if self.a[index] == False:
				return False
		return True

	def additem(self, item):
		base = self.h(item)
		added = self.g(item)
		for i in range(1, self.k + 1):
			index = ((base + i * added) % self.slice) + (i - 1) * self.slice
			self.a[index] = True

	def addall(self, items):
		for item in items:
			self.additem(item)

	def to_base64(self):
		return urlsafe_b64encode(self.a.tobytes())

	def ones(self):
		return self.a.count(True)

	def zeros(self):
		return self.a.count(False)

	def or_ones(self, other):
		return (self | other).ones()

	def or_zeros(self, other):
		return (self | other).zeros()

	def and_ones(self, other):
		return (self & other).ones()

	def and_zeros(self, other):
		return (self & other).zeros()

	def xor_ones(self, other):
		return (self ^ other).ones()

	def xor_zeros(self, other):
		return (self ^ other).zeros()

	def encoded_estimate(self):
		return int(floor(- (self.m / self.k) * log(1 - self.ones() / self.m)))

	def intersection_encoded_estimate(self, other):
		return self.encoded_estimate() + other.encoded_estimate() - self.union_encoded_estimate(other)

	def union_encoded_estimate(self, other):
		return int(floor(- (self.m / self.k) * log(1 - self.or_ones(other) / self.m)))

	def jaccard(self, other):
		return self.intersection_encoded_estimate(other) / self.union_encoded_estimate(other)

	def jaccard_distance(self,other):
		return 1.0 - self.intersection_encoded_estimate(other) / self.union_encoded_estimate(other)

	def tanimoto(self, other):
		return self.and_ones(other) / self.or_ones(other)
