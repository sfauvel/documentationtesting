import docAsTest.approvalFile.ApprovalFile;

public class SwitchToReceivedFileAction extends SwitchToFileAction {

    public SwitchToReceivedFileAction() {
        super(ApprovalFile.Status.RECEIVED);
    }

    @Override
    protected String getMenuText() {
        return "Switch to received file";
    }


}
