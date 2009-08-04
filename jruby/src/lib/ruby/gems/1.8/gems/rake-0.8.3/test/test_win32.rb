#!/usr/bin/env ruby

require 'test/unit'
require 'test/rake_test_setup'
require 'test/in_environment'

require 'rake'

class TestWin32 < Test::Unit::TestCase
  include InEnvironment

  Win32 = Rake::Win32
  
  def test_win32_system_dir_uses_appdata_if_defined
    in_environment('RAKE_SYSTEM' => nil, 'APPDATA' => '\\AD') do
      assert_equal "/AD/Rake", Win32.win32_system_dir 
    end
  end

  def test_win32_system_dir_uses_homedrive_otherwise
    in_environment(
      'RAKE_SYSTEM' => nil,
      'APPDATA' => nil,
      'HOMEDRIVE' => "C:",
      "HOMEPATH" => "\\HP"
      ) do
      assert_equal "C:/HP/Rake", Win32.win32_system_dir
    end
  end

  def test_win32_system_dir_uses_userprofile_otherwise
    in_environment(
      'RAKE_SYSTEM' => nil,
      'APPDATA' => nil,
      'HOMEDRIVE' => nil,
      "HOMEPATH" => nil,
      "USERPROFILE" => '\\UP'
      ) do
      assert_equal "/UP/Rake", Win32.win32_system_dir
    end
  end

  def test_win32_system_dir_nil_of_no_env_vars
    in_environment(
      'RAKE_SYSTEM' => nil,
      'APPDATA' => nil,
      'HOMEDRIVE' => nil,
      "HOMEPATH" => nil,
      "USERPROFILE" => nil
      ) do
      assert_raise(Rake::Win32::Win32HomeError) do
        Win32.win32_system_dir
      end
    end
  end

end
