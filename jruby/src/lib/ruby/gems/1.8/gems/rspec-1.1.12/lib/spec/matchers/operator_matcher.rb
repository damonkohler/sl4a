module Spec
  module Matchers

    class OperatorMatcher
      class << self
        def registry
          @registry ||= Hash.new {|h,k| h[k] = {}}
        end

        def register(klass, operator, matcher)
          registry[klass][operator] = matcher
        end

        def get(klass, operator)
          registry[klass][operator]
        end
      end

      def initialize(actual)
        @actual = actual
      end

      def self.use_custom_matcher_or_delegate(operator)
        define_method(operator) do |expected|
          if matcher = OperatorMatcher.get(@actual.class, operator)
            @actual.send(::Spec::Matchers.last_should, matcher.new(expected))
          else
            ::Spec::Matchers.last_matcher = self
            @operator, @expected = operator, expected
            __delegate_operator(@actual, operator, expected)
          end
        end
      end

      ['==', '===', '=~', '>', '>=', '<', '<='].each do |operator|
        use_custom_matcher_or_delegate operator
      end

      def fail_with_message(message)
        Spec::Expectations.fail_with(message, @expected, @actual)
      end

      def description
        "#{@operator} #{@expected.inspect}"
      end

    end

    class PositiveOperatorMatcher < OperatorMatcher #:nodoc:
      def __delegate_operator(actual, operator, expected)
        return true if actual.__send__(operator, expected)
        if ['==','===', '=~'].include?(operator)
          fail_with_message("expected: #{expected.inspect},\n     got: #{actual.inspect} (using #{operator})") 
        else
          fail_with_message("expected: #{operator} #{expected.inspect},\n     got: #{operator.gsub(/./, ' ')} #{actual.inspect}")
        end
      end

    end

    class NegativeOperatorMatcher < OperatorMatcher #:nodoc:
      def __delegate_operator(actual, operator, expected)
        return true unless actual.__send__(operator, expected)
        return fail_with_message("expected not: #{operator} #{expected.inspect},\n         got: #{operator.gsub(/./, ' ')} #{actual.inspect}")
      end

    end

  end
end
