import docAsTest.approvalFile.ApprovalFile;

public class SwitchToApprovedFileAction extends SwitchToFileAction {

    public SwitchToApprovedFileAction() {
        super(ApprovalFile.Status.APPROVED);
    }

    @Override
    protected String getMenuText() {
        return "Switch to approved file";
    }

}