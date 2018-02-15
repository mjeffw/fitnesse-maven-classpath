package fitnesse.wikitext.widgets;

import java.io.File;
import java.util.Collection;
import java.util.List;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.*;

/**
 * FitNesse SymbolType implementation which enables Maven classpath integration
 * for FitNesse.
 */
public class MavenClasspathSymbolType extends SymbolType implements Rule, Translation, PathsProvider {

    private MavenClasspathExtractor mavenClasspathExtractor;

    public MavenClasspathSymbolType() {
	super("MavenClasspathSymbolType");
	this.mavenClasspathExtractor = new MavenClasspathExtractor();

	wikiMatcher(new Matcher().startLineOrCell().string("!pomFile"));

	wikiRule(this);
	htmlTranslation(this);
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
	List<String> classpathElements = getClasspathElements(symbol);

	String classpathForRender = "";
	// for (String element : classpathElements) {
	// classpathForRender += HtmlUtil.metaText("classpath: " + element) +
	// HtmlUtil.BRtag;
	//
	// }

	// SKETCHY: I hardcoded the BR tag in this; I don't know what the replacement is
	// for HtmlUtils.BRTag
	for (String element : classpathElements) {
	    HtmlTag result = new HtmlTag("span", "classpath: " + element);
	    result.addAttribute("class", "meta");
	    classpathForRender += result.htmlInline() + "<br/>";
	}

	return classpathForRender;
    }

    private List<String> getClasspathElements(Symbol symbol) {
	String pomFile = symbol.childAt(0).getContent();
	String scope = MavenClasspathExtractor.DEFAULT_SCOPE;

	if (pomFile.contains("@")) {
	    String[] s = pomFile.split("@");
	    pomFile = s[0];
	    scope = s[1];
	}

	return mavenClasspathExtractor.extractClasspathEntries(new File(pomFile), scope);
    }

    @Override
    public Maybe<Symbol> parse(Symbol symbol, Parser parser) {
	Symbol next = parser.moveNext(1);

	if (!next.isType(SymbolType.Whitespace))
	    return Symbol.nothing;

	symbol.add(parser.moveNext(1).getContent());

	return new Maybe<Symbol>(symbol);
    }

    @Override
    public boolean matchesFor(SymbolType symbolType) {
	return symbolType instanceof Path || super.matchesFor(symbolType);
    }

    /**
     * Exposed for testing
     */
    protected void setMavenClasspathExtractor(MavenClasspathExtractor mavenClasspathExtractor) {
	this.mavenClasspathExtractor = mavenClasspathExtractor;
    }

    @Override
    public Collection<String> providePaths(Translator translator, Symbol symbol) {
	return getClasspathElements(symbol);
    }
}
