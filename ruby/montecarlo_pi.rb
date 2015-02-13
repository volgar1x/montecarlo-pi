class MontecarloPi
  def self.sample(n)
    rng = Random.new

    inside = 0

    for i in 1..n
      x = rng.rand
      y = rng.rand

      z = x*x+y*y

      if z < 1.0
        inside += 1
      end
    end

    inside
  end
end

#https://gist.github.com/tkareine/739662
class CountDownLatch
  attr_reader :count
 
  def initialize(to)
    @count = to.to_i
    raise ArgumentError, "cannot count down from negative integer" unless @count >= 0
    @lock = Mutex.new
    @condition = ConditionVariable.new
  end
 
  def count_down
    @lock.synchronize do
      @count -= 1 if @count > 0
      @condition.broadcast if @count == 0
    end
  end
 
  def wait
    @lock.synchronize do
      @condition.wait(@lock) while @count > 0
    end
  end
end

n = 100000000
c = 4
iters = (n/c).to_i

syn = CountDownLatch.new(c)
ths = []
c.times do
  ths << Thread.new do
    result = MontecarloPi.sample(iters)
    syn.count_down
    result
  end
end

syn.wait
result = ths.inject(0) { |acc, x| acc + x.value }

pi = result.to_f / n.to_f * 4.0
puts(pi)

