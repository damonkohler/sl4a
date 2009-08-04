require 'benchmark'

def bench_class_definition(bm)
  bm.report("1m class Foo; end") {
    class << self
      1_000_000.times {
        class Foo; end
      }
    end
  }
end

if $0 == __FILE__
  (ARGV[0] || 10).to_i.times { Benchmark.bm(40) {|bm| bench_class_definition(bm)} }
end
