import System.IO
import Data.Char
import Data.List
import Text.Printf


calc_sum [] = 0
calc_sum (x:xs) = x+(sum xs)

test_calc_sum :: [Integer] -> String
test_calc_sum values = printf "calc_sum %s = %d\n" (show values) (calc_sum values)

tests = unlines [
        "= calc_sum\n\n",
        test_calc_sum [],
        test_calc_sum [2],
        test_calc_sum [2, 3]
        ]

main = do
    writeFile "docs/_test_calculator.adoc" tests
