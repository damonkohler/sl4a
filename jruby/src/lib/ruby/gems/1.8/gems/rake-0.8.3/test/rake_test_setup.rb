# Common setup for all test files.

begin
  require 'rubygems'
  gem 'flexmock'
rescue LoadError
  # got no gems
end

require 'flexmock/test_unit'
