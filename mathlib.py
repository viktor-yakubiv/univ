from fractions import Fraction as Fr
from itertools import count, takewhile
from decimal import Decimal, localcontext
from math import factorial, sin, pi


def bernoulli(n):
    # fucking hack
    # if (n == 1):
    #     return Fr(-1, 2)

    numbers = [0] * (n + 1)
    for k in range(n + 1):
        numbers[k] = Fr(1, k + 1)
        for i in range(k, 0, -1):
            numbers[i - 1] = i * (numbers[i - 1] - numbers[i])
    return numbers[0]


def bernoulli_gen():
    A, m = [], 0
    while True:
        A.append(Fr(1, m + 1))
        for j in range(m, 0, -1):
            A[j - 1] = j*(A[j - 1] - A[j])
        yield A[0]
        m += 1


def cosec_step(x):
    with localcontext() as ctx:
        for k in count():
            sign = ctx.power(-1, k + 1)
            pow_2 = ctx.power(2, 2 * k - 1)
            pow_x = ctx.power(x, 2 * k - 1)
            b = bernoulli(2 * k)
            yield (sign * 2 * (pow_2 - 1) * b.numerator / b.denominator * pow_x) / factorial(2 * k)

def cosec_sum(x):
    total = 0
    k = 0
    for step in cosec_step(x):
        total += step
        k += 1
        yield total

def cosec_dbg(x, precision=0.0001, step_limit=1000):
    # process argument
    x = x % (2 * pi)
    if x == 0 or abs(x) == pi:
        raise ValueError('x should not be ecual 0 or pi*N')
    x = Decimal(x)

    iter_count = 0
    result = 0
    expected = 1 / sin(x)
    result_checker = lambda current: abs(current - result) > precision
    for current in takewhile(result_checker, cosec_sum(x)):
        iter_count += 1
        result = current

    print(iter_count)
    return float(result), iter_count

def cosec(x, **kwargs):
    result, _ = cosec_dbg(x, **kwargs)
    return result


if __name__ == '__main__':
    def test_bernoulli():
        bn = [(i, bernoulli(i)) for i in range(61)]
        bn = [(i, b) for i, b in bn if b]
        width = max(len(str(b.numerator)) for i, b in bn)
        for i, b in bn:
            print('B(%2i) = %*i/%i' % (i, width, b.numerator, b.denominator))

    def test_bernoulli2():
        bn2 = [ix for ix in zip(range(61), bernoulli_gen())]
        bn2 = [(i, b) for i, b in bn2 if b]
        width = max(len(str(b.numerator)) for i, b in bn2)
        for i,b in bn2:
            print('B(%2i) = %*i/%i' % (i, width, b.numerator, b.denominator))

    # test_bernoulli()
    # test_bernoulli2()

    # print(cosec(1, 0.0001), 1 / sin(pi / 2))
    # print(cosec(pi), 1 / sin(pi))
    # print(cosec(pi))
    # print(cosec(2 * pi))
    # print(cosec(-pi))
    # print(cosec(2 * pi))
    # print(cosec(0))
