<xsl:stylesheet	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- 
	Is there some way to magically un-indent <pre> sections so that we
	can keep them in formatted XML, but show them correctly in HTML?
 -->
<xsl:template match="example">
	<p/>
	<center>
	<table width="100%" cellpadding="5" border="1">
	<tr><td	bgcolor="#dfdfdc">
	<pre><xsl:value-of select='.'/></pre></td></tr>
	</table>
	</center>
	<p/>
</xsl:template>

</xsl:stylesheet>
