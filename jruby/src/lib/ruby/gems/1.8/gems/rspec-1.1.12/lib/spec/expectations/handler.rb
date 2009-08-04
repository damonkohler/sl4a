module Spec
  module Expectations
    class InvalidMatcherError < ArgumentError; end        
    
    class ExpectationMatcherHandler        
      def self.handle_matcher(actual, matcher, &block)
        ::Spec::Matchers.last_should = :should
        ::Spec::Matchers.last_matcher = matcher

        return ::Spec::Matchers::PositiveOperatorMatcher.new(actual) if matcher.nil?

        match = matcher.matches?(actual, &block)
        ::Spec::Expectations.fail_with(matcher.failure_message) unless match
        match
      end
    end

    class NegativeExpectationMatcherHandler
      def self.handle_matcher(actual, matcher, &block)
        ::Spec::Matchers.last_should = :should_not
        ::Spec::Matchers.last_matcher = matcher

        return ::Spec::Matchers::NegativeOperatorMatcher.new(actual) if matcher.nil?
        
        match = matcher.matches?(actual, &block)
        ::Spec::Expectations.fail_with(matcher.negative_failure_message) if match
        match
      end
    end
  end
end

