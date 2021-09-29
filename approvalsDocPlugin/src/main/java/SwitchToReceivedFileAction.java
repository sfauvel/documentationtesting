public class SwitchToReceivedFileAction extends SwitchToFileAction {

    public SwitchToReceivedFileAction() {
        super(ApprovalFile.Status.RECEIVED);
    }

    protected String getMenuText() {
        return "Switch to received file";
    }


}
