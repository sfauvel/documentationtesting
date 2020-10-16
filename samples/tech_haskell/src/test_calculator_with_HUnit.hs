import Test.HUnit

calc_sum [] = 0
calc_sum (x:xs) = x+(sum xs)

tests = TestList [(TestCase (assertEqual "0 = sum []" 0 (calc_sum []))),
                  (TestCase (assertEqual "2 = sum [2]" 2 (calc_sum [2]))),
                  (TestCase (assertEqual "5 = sum [2,3]" 5 (calc_sum [2,3])))]

-- shortcut to run the tests
main = runTestTT tests
