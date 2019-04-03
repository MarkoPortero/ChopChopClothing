package com.example.markporter.chopchopclothing;

public class ClothingInformation {

    private String PictureLocation;
    private String Price;
    private String ProductID;
    private String ProductName;
    private String PurchaseLink;

    public ClothingInformation() {
    }

    public String getPictureLocation() {
        return PictureLocation;
    }

    public void setPictureLocation(String pictureLocation) {
        PictureLocation = pictureLocation;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProduct_ID(String productID) {
        ProductID = productID;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProduct_Name(String productName) {
        ProductName = productName;
    }

    public String getPurchaseLink() {
        return PurchaseLink;
    }

    public void setPurchase_Link(String purchaseLink) {
        PurchaseLink = purchaseLink;
    }
}
