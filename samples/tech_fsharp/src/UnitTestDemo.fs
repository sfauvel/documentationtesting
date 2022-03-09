module MyDemo.Tests

open NUnit.Framework
open System.IO
open System
open System.Runtime.CompilerServices
open System.Runtime.InteropServices
open Approvals

[<SetUp>]
let Setup () =
    ()

[<Test>]
let Simple_demo () =
    "ToLower on 'C': " + "C".ToLower()
    |> Doc().verify
