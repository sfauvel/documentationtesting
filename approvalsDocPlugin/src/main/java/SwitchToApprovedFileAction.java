public class SwitchToApprovedFileAction extends SwitchToFileAction {

    public SwitchToApprovedFileAction() {
        super(ApprovalFile.Status.APPROVED);
    }

    protected String getMenuText() {
        return "Switch to approved file";
    }

}