#
#   help.rb - helper using ri
#   	$Release Version: 0.9.5$
#   	$Revision: 2062 $
#   	$Date: 2006-06-10 14:14:15 -0500 (Sat, 10 Jun 2006) $
#
# --
#
#   
#

require 'rdoc/ri/ri_driver'

module IRB
  module ExtendCommand
    module Help
      begin
        @ri = RiDriver.new
      rescue SystemExit
      else
        def self.execute(context, *names)
          names.each do |name|
            begin
              @ri.get_info_for(name.to_s)
            rescue RiError
              puts $!.message
            end
          end
          nil
        end
      end
    end
  end
end
