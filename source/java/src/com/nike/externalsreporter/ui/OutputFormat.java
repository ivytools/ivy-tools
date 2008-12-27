package com.nike.externalsreporter.ui;


/** Enum for the variouis types of output. */
public enum OutputFormat
{
    SVG("SVG", "svg", ".svg"),
    PNG("PNG", "png", ".png"),
    PDF("PDF", "epdf", ".pdf");

    private final String displayLabel; // for debug only
    private final String extension; // for debug only
    private final String type; // for debug only


    OutputFormat(String displayLabel,
                 String type,
                 String extension)
    {
        this.displayLabel = displayLabel;
        this.type = type;
        this.extension = extension;
    }


    @Override
    @SuppressWarnings({"RefusedBequest"})
    public String toString()
    {
        return "OutputFormat{" + "type='" + type + "'" + "}";
    }

    public String getDisplayLabel()
    {
        return displayLabel;
    }


    public String getExtension()
    {
        return extension;
    }


    public String getType()
    {
        return type;
    }
}
