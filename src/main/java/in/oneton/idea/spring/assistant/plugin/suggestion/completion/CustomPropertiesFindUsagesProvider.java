package in.oneton.idea.spring.assistant.plugin.suggestion.completion;

import com.github.eltonsandre.plugin.idea.spring.assistant.common.Constants;
import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.lang.properties.parsing.PropertiesWordsScanner;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import in.oneton.idea.spring.assistant.plugin.misc.GenericUtil;
import in.oneton.idea.spring.assistant.plugin.misc.PsiCustomUtil;
import in.oneton.idea.spring.assistant.plugin.suggestion.SuggestionNode;
import in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author zhoumengjie02, darren
 */
public class CustomPropertiesFindUsagesProvider implements FindUsagesProvider {

    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new PropertiesWordsScanner();
    }

    @Override
    public boolean canFindUsagesFor(@NotNull final PsiElement psiElement) {
        return this.findElement(psiElement) != null;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull final PsiElement psiElement) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @NotNull
    @Override
    public String getType(@NotNull final PsiElement element) {
        return PropertiesBundle.message("terms.property");
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull final PsiElement element) {

        final ReferenceProxyElement proxyElement = this.findElement(element);

        if (proxyElement == null) {
            return "Not Found";
        }

        return proxyElement.getTarget().getDocumentationForKey(PsiCustomUtil.findModule(element),
                proxyElement.getNodeNavigationPathDotDelimited());

    }

    @NotNull
    @Override
    public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
        return this.getDescriptiveName(element);
    }

    private ReferenceProxyElement findElement(final PsiElement myElement) {
        final Module module = PsiCustomUtil.findModule(myElement);
        if (Objects.isNull(module)) {
            return null;
        }

        final var service = SuggestionService.getInstance(module);
        final String value = myElement.getText();

        final List<SuggestionNode> matchedNodesFromRootTillLeaf = service.findMatchedNodesRootTillEnd(GenericUtil.getAncestralKey(value));
        if (Objects.isNull(matchedNodesFromRootTillLeaf)) {
            return null;
        }

        final String targetNavigationPathDotDelimited = matchedNodesFromRootTillLeaf.stream()
                .map(v -> v.getNameForDocumentation(module))
                .collect(Collectors.joining(Constants.PROP_DOT));

        final PsiFile file = myElement.getContainingFile();
        final SuggestionNode target = matchedNodesFromRootTillLeaf.get(matchedNodesFromRootTillLeaf.size() - 1);
        return new ReferenceProxyElement(file.getManager(), file.getLanguage(), targetNavigationPathDotDelimited, target, false, value);
    }

}