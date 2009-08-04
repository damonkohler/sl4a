require "android"
name = android.getInput("Hello!", "What is your name?")
android.makeToast("Hello, " .. name.result)
