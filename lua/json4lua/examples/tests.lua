--[[
Some basic tests for JSON4Lua.
]]--

--- Compares two tables for being data-identical.
function compareData(a,b)
  if (type(a)=='string' or type(a)=='number' or type(a)=='boolean' or type(a)=='nil') then return a==b end
  -- After basic data types, we're only interested in tables
  if (type(a)~='table') then return true end
  -- Check that a has everything b has
  for k,v in pairs(b) do
    if (not compareData( a[k], v ) ) then return false end
  end
  for k,v in pairs(a) do
    if (not compareData( v, b[k] ) ) then return false end
  end
  return true
end

---
-- Checks that our compareData function works properly
function testCompareData()
  s = "name"
  r = "name"
  assert(compareData(s,r))
  assert(not compareData('fred',s))
  assert(not compareData(nil, s))
  assert(not compareData("123",123))
  assert(not compareData(false, nil))
  assert(compareData(true, true))
  assert(compareData({1,2,3},{1,2,3}))
  assert(compareData({'one',2,'three'},{'one',2,'three'}))
  assert(not compareData({'one',2,4},{4,2,'one'}))
  assert(compareData({one='ichi',two='nichi',three='san'}, {three='san',two='nichi',one='ichi'}))
  s = { one={1,2,3}, two={one='hitotsu',two='futatsu',three='mitsu'} } 
  assert(compareData(s,s))
  t = { one={1,2,3}, two={one='een',two='twee',three='drie'} } 
  assert(not compareData(s,t))
end

testCompareData()
  
--
--
-- Performs some perfunctory tests on JSON module
function testJSON4Lua()
  json = require('json')
  
  if nil then
  -- Test encodeString
  s = [["\"
]]
  r = json._encodeString(s)
  assert(r=='\\"\\\\\\"\\n')
  s = [["""\\\"]]
  r = json._encodeString(s)
  assert(r==[[\"\"\"\\\\\\\"]])
  
  end 
  
  -- Test encode for basic strings (complicated strings)
  s = [[Hello, Lua!]]
  r = json.encode(s)
  assert(r=='"Hello, Lua!"')
  s = [["\"
]]
  r = json.encode(s)
  assert(r=='\"\\"\\\\\\"\\n\"')
  s = [["""\\\"]]
  r = json.encode(s)
  assert(r==[["\"\"\"\\\\\\\""]])
  
  -- Test encode for numeric values
  s = 23
  r = json.encode(s)
  assert(r=='23')
  s=48.123
  r = json.encode(s)
  assert(r=='48.123')
  
  -- Test encode for boolean values
  assert(json.encode(true)=='true')
  assert(json.encode(false)=='false')
  assert(json.encode(nil)=='null')

  -- Test encode for arrays
  s = {1,2,3}
  r = json.encode(s)
  assert(r=="[1,2,3]")
  s = {9,9,9}
  r = json.encode(s)
  assert(r=="[9,9,9]")
  
  -- Complex array test
  s = { 2, 'joe', false, nil, 'hi' }
  r = json.encode(s)
  assert(r=='[2,"joe",false,null,"hi"]')
  
  -- Test encode for tables
  s = {Name='Craig',email='craig@lateral.co.za',age=35}
  r = json.encode(s)
  -- NB: This test can fail because of order: need to test further once
  -- decoding is supported.
  assert(r==[[{"age":35,"Name":"Craig","email":"craig@lateral.co.za"}]])
  
  -- Test decode_scanWhitespace
  if nil then
  s = "   \n   \r   \t   "
  e = json._decode_scanWhitespace(s,1)
  assert(e==string.len(s)+1)
  s = " \n\r\t4"
  assert(json._decode_scanWhitespace(s,1)==5)
  
  -- Test decode_scanString
  s = [["Test"]]
  r,e = json._decode_scanString(s,1)
  assert(r=='Test' and e==7)
  s = [["This\nis a \"test"]]
  r = json._decode_scanString(s,1)
  assert(r=="This\nis a \"test")
  
  -- Test decode_scanNumber
  s = [[354]]
  r,e = json._decode_scanNumber(s,1)
  assert(r==354 and e==4)
  s = [[ 4565.23 AND OTHER THINGS ]]
  r,e = json._decode_scanNumber(s,2)
  assert(r==4565.23 and e==9)
  s = [[ -23.22 and ]]
  r,e = json._decode_scanNumber(s,2)
  assert(r==-23.22 and e==8)
 
  -- Test decode_scanConstant
  s = "true"
  r,e = json._decode_scanConstant(s,1)
  assert(r==true and e==5)
  s = "  false  "
  r,e = json._decode_scanConstant(s,3)
  assert(r==false and e==8)
  s = "1null6"
  r,e = json._decode_scanConstant(s,2)
  assert(r==nil and e==6)
  
  -- Test decode_scanArray
  s = "[1,2,3]"
  r,e = json._decode_scanArray(s,1)
  assert(compareData(r,{1,2,3}))
  s = [[[  1 ,   3  ,5 , "Fred" , true, false, null, -23 ] ]]
  r,e = json._decode_scanArray(s,1)
  assert(compareData(r, {1,3,5,'Fred',true,false,nil,-23} ) )
  s = "[3,5,null,7,9]"
  r,e = json._decode_scanArray(s,1)
  assert(compareData(r, {3,5,nil,7,9}))
  s = "[3,5,null,7,9,null,null]"
  r,e = json._decode_scanArray(s,1)
  assert(compareData(r, {3,5,nil,7,9,nil,nil}))
  
  end
  
  -- Test decode_scanObject
  s = [[ {"one":1, "two":2, "three":"three", "four":true} ]]
  r,e = json.decode(s)
  assert(compareData(r,{one=1,two=2,three='three',four=true}))
  s = [[ { "one" : { "first":1,"second":2,"third":3}, "two":2, "three":false } ]]
  r,e = json.decode(s)
  assert(compareData(r, {one={first=1,second=2,third=3},two=2,three=false}))
  s = [[ { "primes" : [2,3,5,7,9], "user":{"name":"craig","age":35,"programs_lua":true},
    "lua_is_great":true } ]]
  r,e = json.decode(s)
  assert(compareData(r, {primes={2,3,5,7,9},user={name='craig',age=35,programs_lua=true},lua_is_great=true}))
  
  -- Test json.null management
  t = { 1,2,json.null,4 }
  assert( json.encode(t)=="[1,2,null,4]" )
  t = {x=json.null }
  r = json.encode(t)
  assert( json.encode(t) == '{"x":null}' )
  
  -- Test comment decoding
  s = [[ /* A comment
            that spans
            a few lines
         */
         "test"
      ]]
  r,e = json.decode(s)
  assert(r=='test',"Comment decoding failed")
end

testJSON4Lua()

print("JSON4Lua tests completed successfully")