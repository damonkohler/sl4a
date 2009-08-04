<xsl:stylesheet	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Should move these common xsl includes to a common area 
	Right now they are in manua/xsl
-->
<!--
	identity.xsl: Pass HTML (and other unknown markup) through.
	Note: this requires that later we match *all* of our own tags and
	explicitly remove them from output.  See match="section".
-->
<xsl:import href="../manual/xsl/identity.xsl"/>
<!-- Handle example text 
<xsl:import href="../manual/xsl/example.xsl"/>
-->

<!-- Parameters -->
<!--xsl:param name="multipage"/-->
<!--xsl:param name="imagedir"/-->

<!-- Output directives -->
<xsl:output method="xhtml" indent="yes"/>

<!-- 
	Root
	Override / in other imports
-->
<xsl:template match="/">
	<xsl:apply-templates/>
</xsl:template>

<!-- 
	FAQ
-->
<xsl:template match="FAQ">
	<html>
	<head><title>BeanShell FAQ</title></head>
	<body bgcolor="#ffffff">
		<h1>BeanShell FAQ</h1>
		<xsl:apply-templates/>
	</body>
	</html>
</xsl:template>

<xsl:template match="category">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="body">
	<xsl:apply-templates/>
</xsl:template>

<!-- 
	entry
-->
<xsl:template match="entry">
	<xsl:element name="a">
		<xsl:attribute name="name"><xsl:number level="any" count="entry"/></xsl:attribute>
		<h2><xsl:value-of select="question"/></h2>
	</xsl:element>
	<xsl:apply-templates select="answer"/>
	<hr/>
</xsl:template>

<xsl:template match="answer">
	<xsl:apply-templates/>
</xsl:template>

<!--
	Table of contents
-->
<xsl:template match="head">
	Authors:
	<br/>
	<xsl:for-each select="authors/author">
		<xsl:value-of select="@name"/>(<xsl:value-of select="@email"/>)<br/>
	</xsl:for-each>
	<p/>
	<ul>
	<xsl:for-each select="/FAQ/body/category/entry">
		<li>
		<xsl:element name="a">
			<xsl:attribute name="href">#<xsl:number level="any" count="entry"/></xsl:attribute>
			<xsl:value-of select="question"/>
		</xsl:element>
		</li>
	</xsl:for-each>
	</ul>
	<hr/>
</xsl:template>

</xsl:stylesheet>

