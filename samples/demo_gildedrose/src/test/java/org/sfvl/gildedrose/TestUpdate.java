package org.sfvl.gildedrose;

import fr.xebia.katas.gildedrose.Inn;
import fr.xebia.katas.gildedrose.Item;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class TestUpdate {

    @Test
    public void test_update_item() {
        Inn inn = new Inn();
        StringBuffer output = new StringBuffer();
        for (int i = 1; i < 20; i++) {
            for (Item item : inn.getItems()) {
                output.append(String.format("%s,%d,%d\n", item.getName(), item.getSellIn(), item.getQuality()));
            }
            inn.updateQuality();
        }

        Approvals.verify(output);
    }

}
