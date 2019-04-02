package br.com.mdd4clinicalscores.mews;

import java.io.Serializable;
import javax.swing.ImageIcon;

public class CustomData implements Serializable {
    private static final long serialVersionUID = 6093226637618022646L;
    private String caption;
    private ImageIcon icon;

    public CustomData() {
    }

    public CustomData(String caption, String icon) {
        this.caption = caption;

        this.icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(icon));
    }

    public String getCaption() {
        return caption;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        String firstPart  = "Caption: " + caption;
        String secondPart = "\nIcon Height: " + icon.getIconHeight();
        String thirdPart  = "\nIcon Width: " + icon.getIconWidth();

        return firstPart + secondPart + thirdPart;
    }
}
