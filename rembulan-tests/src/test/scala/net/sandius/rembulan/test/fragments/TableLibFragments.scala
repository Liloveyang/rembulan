/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}

object TableLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (TableContext) {

    about ("table.concat") {

      program ("""return table.concat()""") failsWith "bad argument #1 to 'concat' (table expected, got no value)"
      program ("""return table.concat(nil)""") failsWith "bad argument #1 to 'concat' (table expected, got nil)"
      program ("""return table.concat({}, nil)""") succeedsWith ("")
      program ("""return table.concat({}, false)""") failsWith "bad argument #2 to 'concat' (string expected, got boolean)"
      program ("""return table.concat({}, true, true)""") failsWith "bad argument #2 to 'concat' (string expected, got boolean)"
      program ("""return table.concat({}, true, true, false)""") failsWith "bad argument #2 to 'concat' (string expected, got boolean)"

      in (FullContext) {
        // strings have the __index metamethod, but concat doesn't care about them
        program ("""return table.concat("hello", " ")""") failsWith "bad argument #1 to 'concat' (table expected, got string)"
      }

      program ("""return table.concat({}, "", true, false)""") failsWith "bad argument #3 to 'concat' (number expected, got boolean)"
      program ("""return table.concat({}, "", nil, false)""") failsWith "bad argument #4 to 'concat' (number expected, got boolean)"
      program ("""return table.concat({}, nil, nil, nil)""") succeedsWith ("")

      program ("""return table.concat({3, 2, 1})""") succeedsWith ("321")
      program ("""return table.concat({3, [4]=0, 2, 1})""") succeedsWith ("3210")
      program ("""return table.concat({3, x=0, 2, 1})""") succeedsWith ("321")

      program ("""return table.concat({[-1]=1, [-2]=2, [0]=0, [2]=-2, [1.0]=-1}, "", -2)""") succeedsWith ("210-1-2")
      program ("""return table.concat({[-1]=1, [-2]=2, [0]=0, [2]=-2, [1.0]=-1})""") succeedsWith ("-1-2")

      program ("""return table.concat({})""") succeedsWith ("")
      program ("""return table.concat({}, "BOO")""") succeedsWith ("")

      program ("""return table.concat({}, "", 50)""") succeedsWith ("")
      program ("""return table.concat({}, "", 0)""") failsWith "invalid value (nil) at index 0 in table for 'concat'"
      program ("""return table.concat({}, "", -1)""") failsWith "invalid value (nil) at index -1 in table for 'concat'"
      program ("""return table.concat({}, "", 1, 20)""") failsWith "invalid value (nil) at index 1 in table for 'concat'"

      program ("""return table.concat({1, 2}, "", 1, 1)""") succeedsWith ("1")
      program ("""return table.concat({1, 2, 3, 4}, "", -2, 3)""") failsWith "invalid value (nil) at index -2 in table for 'concat'"

      program ("""return table.concat({"hello", "world", 0})""") succeedsWith ("helloworld0")
      program ("""return table.concat({"hello", "world", 0}, " ")""") succeedsWith ("hello world 0")

      program ("""return table.concat({"a", 1, "b", 2, "c", 3}, nil)""") succeedsWith ("a1b2c3")
      program ("""return table.concat({"a", 1, "b"}, -0.0)""") succeedsWith ("a-0.01-0.0b")

      program ("""return table.concat({"a", 1, {}})""") failsWith "invalid value (table) at index 3 in table for 'concat'"
      program ("""return table.concat({{}, {}})""") failsWith "invalid value (table) at index 1 in table for 'concat'"

      in (FullContext) {
        program ("""return table.concat({io.stdin})""") failsWith "invalid value (userdata) at index 1 in table for 'concat'"
      }

      // concat uses the __index metamethod on the concatenated table
      program (
        """local mt = {__index = function(t, k) return k end}
          |return table.concat(setmetatable({}, mt), " ", 3, 5)
        """) succeedsWith ("3 4 5")

      // concat uses the __len metamethod on the concatenated table
      program (
        """local mt = {__len = function() return 2 end}
          |return table.concat(setmetatable({5, 4, 3, 2, 1, 0}, mt), " ")
        """) succeedsWith ("5 4")

      program ("""local mt = {__len = 10}; return table.concat(setmetatable({}, mt))""") failsWith "attempt to call a number value"
      program ("""local mt = {__len = function() return "x" end}; return table.concat(setmetatable({}, mt))""") failsWith "object length is not an integer"
      program ("""local mt = {__len = function() return "2" end}; return table.concat(setmetatable({"a", "b", "c"}, mt))""") succeedsWith "ab"
      program (
        """local mt = {__len = function() return "3.0" end}
          |return table.concat(setmetatable({"a", "b", "c"}, mt))
        """) succeedsWith "abc"

      program ("""return table.concat(setmetatable({}, {__len = error("BOOM")}), "", 1, true)""") failsWith "BOOM"
      program ("""return table.concat(setmetatable({}, {__len = error("BOOM")}), true, true)""") failsWith "BOOM"
      program ("""return table.concat(setmetatable({}, {__len = error("BOOM")}), false)""") failsWith "BOOM"

      // concat uses the __index and __len metamethods on the concatenated table
      program (
        """local mt = {__index = function(t, k) return k end; __len = function() return 2 end}
          |return table.concat(setmetatable({}, mt), "_", -2)
        """) succeedsWith ("-2_-1_0_1_2")

      // concat does not use the __tostring metamethod of table elements
      program (
        """local mt = {__tostring = function() return "{}" end}
          |return table.concat({"a", 1, setmetatable({}, mt)})
        """) failsWith "invalid value (table) at index 3 in table for 'concat'"

      // concat does not use the __concat metamethod of table elements
      program (
        """local mt = {__concat = function() return "{}" end}
          |return table.concat({"a", 1, setmetatable({}, mt)})
        """) failsWith "invalid value (table) at index 3 in table for 'concat'"

    }

    about ("table.pack") {

      program ("""return table.pack(xx, yy, zz).n""") succeedsWith (3)
      program ("""return #table.pack(3, 2, 1, 0, -1)""") succeedsWith (5)

    }

    about ("table.unpack") {

      program ("""return table.unpack()""") failsWith "attempt to get length of a nil value"
      program ("""return table.unpack(1)""") failsWith "attempt to get length of a number value"
      program ("""return table.unpack(1,2)""") failsWith "attempt to get length of a number value"
      program ("""return table.unpack(1,2,3)""") failsWith "attempt to index a number value"

      program ("""return table.unpack(1,2.3)""") failsWith "bad argument #2 to 'unpack' (number has no integer representation)"
      program ("""return table.unpack(1,"x")""") failsWith "bad argument #2 to 'unpack' (number expected, got string)"
      program ("""return table.unpack(1,2,"y")""") failsWith "bad argument #3 to 'unpack' (number expected, got string)"

      program ("""local x; return table.unpack(1,x)""") failsWith "attempt to get length of a number value"

      program ("""return table.unpack({1,2,3}, -2, 2)""") succeedsWith (null, null, null, 1, 2)
      program ("""return table.unpack({3,2,1}, 3, 3)""") succeedsWith (1)
      program ("""return table.unpack({3,2,1}, 0, 1)""") succeedsWith (null, 3)
      program ("""return table.unpack({3,2,1}, 0, -1)""") succeedsWith ()
      program ("""return table.unpack({3,2,1}, 10, 12)""") succeedsWith (null, null, null)

      program ("""return table.unpack({3,2,1,0,-1}, 2)""") succeedsWith (2, 1, 0, -1)
      program ("""return table.unpack({1,0,-1})""") succeedsWith (1, 0, -1)

      program ("""return table.unpack("nono")""") failsWith "attempt to index a string value"

      in (FullContext) {

        program ("""return table.unpack("hello")""") succeedsWith (null, null, null, null, null)
        program ("""return table.unpack("1","2","3")""") succeedsWith (null, null)

      }

      about ("__len metamethod") {

        program ("""return table.unpack(setmetatable({}, { __len = 3 }))""") failsWith "attempt to call a number value"

        program ("""return table.unpack(setmetatable({}, { __len = function() error("boom") end }), -1, 1)""") succeedsWith (null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() error("boom") end }), -1)""") failsWithLuaError "boom"

        program ("""return table.unpack(setmetatable({}, { __len = function() return 2 end }), -1)""") succeedsWith (null, null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() return "2" end }), -1)""") succeedsWith (null, null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() return "2.0" end }), -1)""") succeedsWith (null, null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() return "boo" end }), 1)""") failsWith "object length is not an integer"

      }

      about ("__index metamethod") {

        program ("""return table.unpack(setmetatable({}, { __index = 3 }), 1, 2)""") failsWith "attempt to index a number value"

        program ("""local mt = { __index = function(t, k) local x = k or 0; return x * x end }
                   |return table.unpack(setmetatable({}, mt), -1, 3)
                 """) succeedsWith (1, 0, 1, 4, 9)

      }

      about ("__len and __index metamethods") {

        program ("""local mt = {
                   |  __index = function(t, k) local x = k or 0; return x * x end,
                   |  __len = function() return 5 end
                   |}
                   |return table.unpack(setmetatable({}, mt))
                 """) succeedsWith (1, 4, 9, 16, 25)

      }

    }

  }

}
