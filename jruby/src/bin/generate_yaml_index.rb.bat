@echo off
call "%~dp0jruby" -S generate_yaml_index.rb %*
