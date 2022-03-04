module MyDemo.Tests

open NUnit.Framework
open System.IO
open System
open System.Runtime.CompilerServices
open System.Runtime.InteropServices


type Caller() =
    member _.Name([<CallerMemberName; Optional; DefaultParameterValue("")>] memberName: string) =
        memberName

type ApprovalType =
    | Approved
    | Received

type Doc() =
    let extension(approvalType: ApprovalType) =
        match approvalType with
        | Approved -> "approved"
        | Received -> "received"

    let doc (file: string, approvalType: ApprovalType) =
        Directory.GetParent(__SOURCE_DIRECTORY__).ToString() + "/docs/" + __SOURCE_FILE__ + "_" + file + "." + extension(approvalType)

    let received (file: string) = doc(file, Received)
    let approved (file: string) = doc(file, Approved)

    member _.verify(text: string
                , [<CallerMemberName; Optional; DefaultParameterValue("")>] filename: string) =

        let full_text = "= " + filename + "\n\n" + text

        use sw = new StreamWriter(received filename, false)
        sw.Write(full_text)
        sw.Close()

        if not(File.Exists(approved filename)) then
            Assert.Fail("No approved file for: " + filename)
        else
            Assert.That(full_text, Is.EqualTo(System.IO.File.ReadAllText(approved filename)))

        File.Delete(received filename);

[<SetUp>]
let Setup () =
    ()

[<Test>]
let Simple_demo () =
    Doc().verify("ToLower on 'C': " + "C".ToLower())