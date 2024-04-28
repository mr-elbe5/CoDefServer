<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica, sans-serif">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm" margin="1cm">
          <fo:region-body margin-bottom="1cm"/>
          <fo:region-after extent="1cm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="simpleA4" id="page_sequence">
        <fo:static-content flow-name="xsl-region-after">
          <xsl:call-template name="footer"/>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <xsl:call-template name="body"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template name="body">
    <xsl:apply-templates select="*" />
  </xsl:template>

  <xsl:template name="footer">
    <fo:block-container border-color="#333333" border-top-style="solid">
      <fo:table table-layout="fixed" width="100%" font-size="10pt" margin-top="2mm">
        <fo:table-column column-width="50%"/>
        <fo:table-column column-width="50%"/>
        <fo:table-body>
          <fo:table-row>
            <fo:table-cell>
              <fo:block>
                <xsl:value-of select="//footer/docAndDate"/>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block text-align="right">
                Seite
                <fo:page-number/>
                von
                <fo:page-number-citation-last ref-id="page_sequence"/>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:block-container>
  </xsl:template>

  <xsl:template match="topheader">
    <fo:block-container background-color="#f0f0f0" padding="0.1cm" margin-bottom="0.1cm" text-align="center" border-color="#333333" border-bottom-style="solid" border-top-style="solid">
      <fo:block font-weight="bold" font-size="15pt">
        <xsl:value-of select="text"/>
      </fo:block>
    </fo:block-container>
  </xsl:template>

  <xsl:template match="subheader">
    <fo:block-container background-color="#f8f8f8" padding="0.1cm" margin-bottom="0.1cm" text-align="center" border-color="#333333" border-bottom-style="solid" border-top-style="solid">
      <fo:block font-weight="bold" font-size="12pt">
        <xsl:value-of select="text"/>
      </fo:block>
    </fo:block-container>
  </xsl:template>

  <xsl:template match="textline">
    <fo:block-container padding="0.1cm" >
      <fo:block>
        <xsl:value-of select="text"/>
      </fo:block>
    </fo:block-container>
  </xsl:template>

  <xsl:template match="image">
    <fo:block-container padding="0.1cm" >
      <fo:block margin-left="1.5mm" border-color="#333333" border-bottom-style="solid" margin-bottom="1mm">
        <fo:external-graphic content-width="scale-to-fit" width="18cm">
          <xsl:attribute name="src">
            <xsl:value-of select="src" />
          </xsl:attribute>
        </fo:external-graphic>
      </fo:block>
    </fo:block-container>
  </xsl:template>

  <xsl:template match="tablecell">
    <fo:table-cell>
      <fo:block padding="1mm">
        <xsl:value-of select="text"/>
      </fo:block>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="tablecellbold">
    <fo:table-cell font-weight="bold">
      <fo:block padding="1mm">
        <xsl:value-of select="text"/>
      </fo:block>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="tablecellimage">
    <fo:table-cell>
      <fo:block padding="0.75mm">
        <fo:external-graphic content-width="scale-to-fit">
          <xsl:attribute name="src">
            <xsl:value-of select="src" />
          </xsl:attribute>
          <xsl:attribute name="height">
            <xsl:value-of select="height" />
          </xsl:attribute>
        </fo:external-graphic>
      </fo:block>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="tablerow">
    <fo:table-row>
      <xsl:apply-templates select="tablecell | tablecellbold | tablecellimage"/>
    </fo:table-row>
  </xsl:template>

  <xsl:template match="table2col">
    <fo:block page-break-inside="auto">
      <fo:table table-layout="fixed" width="100%" font-size="10pt">
        <fo:table-column column-width="30%"/>
        <fo:table-column column-width="70%"/>
        <fo:table-body margin-left="1.5mm">
          <xsl:apply-templates select="tablerow"/>
        </fo:table-body>
      </fo:table>
    </fo:block>
  </xsl:template>

  <xsl:template match="table4col">
    <fo:block page-break-inside="auto">
      <fo:table table-layout="fixed" width="100%" font-size="10pt">
        <fo:table-column column-width="20%"/>
        <fo:table-column column-width="30%"/>
        <fo:table-column column-width="20%"/>
        <fo:table-column column-width="30%"/>
        <fo:table-body margin-left="1.5mm">
          <xsl:apply-templates select="tablerow"/>
        </fo:table-body>
      </fo:table>
    </fo:block>
  </xsl:template>

  <xsl:template match="table5col">
    <fo:block page-break-inside="auto">
      <fo:table table-layout="fixed" width="100%" font-size="10pt">
        <fo:table-column column-width="20%"/>
        <fo:table-column column-width="20%"/>
        <fo:table-column column-width="20%"/>
        <fo:table-column column-width="20%"/>
        <fo:table-column column-width="20%"/>
        <fo:table-body margin-left="1.5mm">
          <xsl:apply-templates select="tablerow"/>
        </fo:table-body>
      </fo:table>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>
