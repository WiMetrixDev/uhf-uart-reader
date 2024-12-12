package expo.modules.uhfuartreader;

public class EPCModel {
    String EPC;
    String Barcode;
    String Size;

    public EPCModel(String EPC, String barcode, String size) {
        this.EPC = EPC;
        Barcode = barcode;
        Size = size;
    }

    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public String getSize() {
        return Size;
    }

    public void setSize(String size) {
        Size = size;
    }
}
