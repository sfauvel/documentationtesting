module Approvals

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

let extension(approvalType: ApprovalType) =
    match approvalType with
    | Approved -> "approved"
    | Received -> "received"

type Doc([<CallerMemberName; Optional; DefaultParameterValue("")>] caller_filename: string,
         [<CallerFilePath; Optional; DefaultParameterValue("")>] source_file_path: string) =

    let doc (file: string, approvalType: ApprovalType) =
        Directory.GetParent(__SOURCE_DIRECTORY__).ToString() + "/docs/_" + file + "." + extension(approvalType) + ".adoc"

    let file_name = Array.last(source_file_path.Split('/')) + "_" + caller_filename

    let approved_file = doc(file_name, Approved)
    let received_file = doc(file_name, Received)

    let write_file(file:string, text: string) =
        use sw = new StreamWriter(file, false)
        sw.Write(text)
        sw.Close()

    member _.verify(text: string) =

        let full_text = "= " + caller_filename.Replace("_", " ") + "\n\n" + text

        if not(File.Exists(approved_file)) then
            write_file(received_file, full_text)
            Assert.Fail("No approved file for: " + approved_file)
        else
            let approved_text = System.IO.File.ReadAllText(approved_file)
            if full_text = approved_text then
                File.Delete(received_file);
            else
                write_file(received_file, full_text)
                Assert.That(full_text, Is.EqualTo(approved_text))
