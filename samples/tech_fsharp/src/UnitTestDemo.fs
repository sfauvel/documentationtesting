module MyDemo.Tests

open NUnit.Framework

let format_to_lower(x:string) =
     "* " + x + " -> " + x.ToLower() + "\n"

[<Test>]
let Simple_demo () =
    "ToLower on string\n\n"
        + format_to_lower("C")
        + format_to_lower("D")
    |> Approvals.Doc().verify
