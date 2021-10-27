package org.sfvl.demo;

class Product {
    TaxType taxType;
    int price;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    int getTaxes() {
        if (taxType == TaxType.TAX_FREE) {
            return 0;
        }
        return (int) Math.round(price * 0.2);
    }
}
