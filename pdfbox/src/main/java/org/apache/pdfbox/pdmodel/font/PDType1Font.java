/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.FontMetric;
import org.apache.fontbox.type1.Type1Font;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.encoding.AFMEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.StandardEncoding;
import org.apache.pdfbox.encoding.Type1Encoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This is implementation of the Type1 Font.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDType1Font extends PDSimpleFont
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDType1Font.class);

    private PDType1CFont type1CFont = null;
    private Type1Font type1font = null;

    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_ROMAN = new PDType1Font("Times-Roman");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_BOLD = new PDType1Font("Times-Bold");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_ITALIC = new PDType1Font("Times-Italic");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_BOLD_ITALIC = new PDType1Font("Times-BoldItalic");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA = new PDType1Font("Helvetica");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_BOLD = new PDType1Font("Helvetica-Bold");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_OBLIQUE = new PDType1Font("Helvetica-Oblique");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_BOLD_OBLIQUE = new PDType1Font("Helvetica-BoldOblique");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER = new PDType1Font("Courier");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_BOLD = new PDType1Font("Courier-Bold");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_OBLIQUE = new PDType1Font("Courier-Oblique");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_BOLD_OBLIQUE = new PDType1Font("Courier-BoldOblique");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font SYMBOL = new PDType1Font("Symbol");
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font ZAPF_DINGBATS = new PDType1Font("ZapfDingbats");

    private static final Map<String, PDType1Font> STANDARD_14 = new HashMap<String, PDType1Font>();
    static
    {
        STANDARD_14.put(TIMES_ROMAN.getBaseFont(), TIMES_ROMAN);
        STANDARD_14.put(TIMES_BOLD.getBaseFont(), TIMES_BOLD);
        STANDARD_14.put(TIMES_ITALIC.getBaseFont(), TIMES_ITALIC);
        STANDARD_14.put(TIMES_BOLD_ITALIC.getBaseFont(), TIMES_BOLD_ITALIC);
        STANDARD_14.put(HELVETICA.getBaseFont(), HELVETICA);
        STANDARD_14.put(HELVETICA_BOLD.getBaseFont(), HELVETICA_BOLD);
        STANDARD_14.put(HELVETICA_OBLIQUE.getBaseFont(), HELVETICA_OBLIQUE);
        STANDARD_14.put(HELVETICA_BOLD_OBLIQUE.getBaseFont(), HELVETICA_BOLD_OBLIQUE);
        STANDARD_14.put(COURIER.getBaseFont(), COURIER);
        STANDARD_14.put(COURIER_BOLD.getBaseFont(), COURIER_BOLD);
        STANDARD_14.put(COURIER_OBLIQUE.getBaseFont(), COURIER_OBLIQUE);
        STANDARD_14.put(COURIER_BOLD_OBLIQUE.getBaseFont(), COURIER_BOLD_OBLIQUE);
        STANDARD_14.put(SYMBOL.getBaseFont(), SYMBOL);
        STANDARD_14.put(ZAPF_DINGBATS.getBaseFont(), ZAPF_DINGBATS);
    }

    /**
     * The static map of the default Adobe font metrics.
     */
    private static final Map<String, FontMetric> afmObjects = Collections.unmodifiableMap(getAdobeFontMetrics());

    private FontMetric afm = null;

    private static Map<String, FontMetric> getAdobeFontMetrics()
    {
        Map<String, FontMetric> metrics = new HashMap<String, FontMetric>();
        addAdobeFontMetric(metrics, "Courier-Bold");
        addAdobeFontMetric(metrics, "Courier-BoldOblique");
        addAdobeFontMetric(metrics, "Courier");
        addAdobeFontMetric(metrics, "Courier-Oblique");
        addAdobeFontMetric(metrics, "Helvetica");
        addAdobeFontMetric(metrics, "Helvetica-Bold");
        addAdobeFontMetric(metrics, "Helvetica-BoldOblique");
        addAdobeFontMetric(metrics, "Helvetica-Oblique");
        addAdobeFontMetric(metrics, "Symbol");
        addAdobeFontMetric(metrics, "Times-Bold");
        addAdobeFontMetric(metrics, "Times-BoldItalic");
        addAdobeFontMetric(metrics, "Times-Italic");
        addAdobeFontMetric(metrics, "Times-Roman");
        addAdobeFontMetric(metrics, "ZapfDingbats");
        
        // PDFBOX-239
        addAdobeFontMetric(metrics, "Arial", "Helvetica");
        addAdobeFontMetric(metrics, "Arial,Bold", "Helvetica-Bold");
        addAdobeFontMetric(metrics, "Arial,Italic", "Helvetica-Oblique");
        addAdobeFontMetric(metrics, "Arial,BoldItalic", "Helvetica-BoldOblique");

        return metrics;
    }

    private static final String resourceRootAFM = "org/apache/pdfbox/resources/afm/";

    private static void addAdobeFontMetric(Map<String, FontMetric> metrics, String name)
    {
        addAdobeFontMetric(metrics, name, name);
    }
    
    private static void addAdobeFontMetric(Map<String, FontMetric> metrics, String name, String filePrefix)
    {
        try
        {
            String resource = resourceRootAFM + filePrefix + ".afm";
            InputStream afmStream = ResourceLoader.loadResource(resource);
            if (afmStream != null)
            {
                try
                {
                    AFMParser parser = new AFMParser(afmStream);
                    FontMetric metric = parser.parse(); 
                    metrics.put(name, metric);
                }
                finally
                {
                    afmStream.close();
                }
            }
        }
        catch (Exception e)
        {
            LOG.error("Something went wrong when reading the adobe afm files", e);
        }
    }

    /**
     * Constructor.
     */
    public PDType1Font()
    {
        super();
        font.setItem(COSName.SUBTYPE, COSName.TYPE1);
    }

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType1Font(COSDictionary fontDictionary)
    {
        super(fontDictionary);
        PDFontDescriptor fd = getFontDescriptor();
        if (fd != null && fd instanceof PDFontDescriptorDictionary)
        {
            // a Type1 font may contain a Type1C font
            PDStream fontFile3 = ((PDFontDescriptorDictionary) fd).getFontFile3();
            if (fontFile3 != null)
            {
                try
                {
                    type1CFont = new PDType1CFont(super.font);
                }
                catch (IOException e)
                {
                    LOG.error("Can't read the embedded Type1C font " + fd.getFontName(), e);
                }
            }

            // or it may contain a PFB
            PDStream fontFile = ((PDFontDescriptorDictionary) fd).getFontFile();
            if (fontFile != null)
            {
                try
                {
                    COSStream stream = fontFile.getStream();
                    int length1 = stream.getInt(COSName.LENGTH1);
                    int length2 = stream.getInt(COSName.LENGTH2);

                    // the PFB embedded as two segments back-to-back
                    byte[] bytes = fontFile.getByteArray();
                    byte[] segment1 = Arrays.copyOfRange(bytes, 0, length1);
                    byte[] segment2 = Arrays.copyOfRange(bytes, length1, length1 + length2);

                    type1font =  Type1Font.createWithSegments(segment1, segment2);
                }
                catch (IOException e)
                {
                    LOG.error("Can't read the embedded Type1 font " + fd.getFontName(), e);
                }
            }
        }
        getEncodingFromFont(getFontEncoding() == null);
    }
    /**
     * Constructor.
     * 
     * @param baseFont The base font for this font.
     */
    public PDType1Font(String baseFont)
    {
        this();
        setBaseFont(baseFont);
        setFontEncoding(new WinAnsiEncoding());
        setEncoding(COSName.WIN_ANSI_ENCODING);
    }

    protected FontMetric getAFM()
    {
        if (afm == null)
        {
            COSBase baseFont = font.getDictionaryObject(COSName.BASE_FONT);
            String name = null;
            if (baseFont instanceof COSName)
            {
                name = ((COSName) baseFont).getName();
                if (name.indexOf("+") > -1)
                {
                    name = name.substring(name.indexOf("+") + 1);
                }

            }
            else if (baseFont instanceof COSString)
            {
                COSString string = (COSString) baseFont;
                name = string.getString();
            }
            if (name != null)
            {
                afm = afmObjects.get(name);
            }
        }
        return afm;
    }

    /**
     * A convenience method to get one of the standard 14 font from name.
     * 
     * @param name The name of the font to get.
     * 
     * @return The font that matches the name or null if it does not exist.
     */
    public static PDType1Font getStandardFont(String name)
    {
        return (PDType1Font) STANDARD_14.get(name);
    }

    /**
     * This will get the names of the standard 14 fonts.
     * 
     * @return An array of the names of the standard 14 fonts.
     */
    public static String[] getStandard14Names()
    {
        return (String[]) STANDARD_14.keySet().toArray(new String[14]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void determineEncoding()
    {
        super.determineEncoding();
        Encoding fontEncoding = getFontEncoding();
        if (fontEncoding == null)
        {
            FontMetric metric = getAFM();
            if (metric != null)
            {
                fontEncoding = new AFMEncoding(metric);
            }
            setFontEncoding(fontEncoding);
        }
    }

    /**
     * Tries to get the encoding for the type1 font.
     *
     */
    private void getEncodingFromFont(boolean extractEncoding)
    {
        if (type1font != null)
        {
            // Fontmatrix
            List<Number> matrixValues = type1font.getFontMatrix();
            if (!matrixValues.isEmpty() && matrixValues.size() == 6)
            {
                COSArray array = new COSArray();
                for (Number value : matrixValues  )
                {
                    array.add(new COSFloat(value.floatValue()));
                }
                fontMatrix = new PDMatrix(array);
            }
            if (extractEncoding)
            {
                // Encoding
                org.apache.fontbox.encoding.Encoding encoding = type1font.getEncoding();
                if (encoding instanceof org.apache.fontbox.encoding.StandardEncoding)
                {
                    setFontEncoding(StandardEncoding.INSTANCE);
                }
                else if (encoding instanceof org.apache.fontbox.encoding.CustomEncoding)
                {
                    Map<Integer,String> codeToName = encoding.getCodeToNameMap();
                    Type1Encoding type1Encoding = new Type1Encoding(codeToName.size());
                    for (Integer code : codeToName.keySet())
                    {
                        type1Encoding.addCharacterEncoding(code, codeToName.get(code));
                    }
                    setFontEncoding(type1Encoding);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(byte[] c, int offset, int length) throws IOException
    {
        if (type1CFont != null && getFontEncoding() == null)
        {
            String character = type1CFont.encode(c, offset, length);
            if (character != null)
            {
                return character;
            }
        }
        return super.encode(c, offset, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int encodeToCID(byte[] c, int offset, int length) throws IOException
    {
        if (type1CFont != null && getFontEncoding() == null)
        {
            return type1CFont.encodeToCID(c, offset, length);
        }
        else
        {
            return super.encodeToCID(c, offset, length);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PDMatrix getFontMatrix()
    {
        if (type1CFont != null)
        {
            return type1CFont.getFontMatrix();
        }
        else
        {
            return super.getFontMatrix();
        }
    }

    /**
     * Returns the embedded Type1C font if available.
     * 
     * @return the Type1C font
     */
    public PDType1CFont getType1CFont()
    {
        return type1CFont;
    }

    /**
     * Returns the embedded Type font if available.
     *
     * @return the Type1 font
     */
    public Type1Font getType1Font()
    {
        return type1font;
    }

    @Override
    public void clear()
    {
        super.clear();
        if (type1CFont != null)
        {
            type1CFont.clear();
            type1CFont = null;
        }
        type1font = null;
        afm = null;
    }
}
