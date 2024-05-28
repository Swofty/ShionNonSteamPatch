package shionnonsteampatch.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.helpers.FontHelper;

import VUPShionMod.ui.WebButton;
import basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard.CardModifierPatches;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class WebButtonPatches
{
    /**
     * The URL String in WebButton is final, so we cannot assign it during the Constructor patch.
     * Save it into its own field.
     */
    @SpirePatch(clz = WebButton.class, method = SpirePatch.CLASS, requiredModId = "VUPShionMod")
    public static class NonFinalUrlField
    {
        public static SpireField<String> newUrl = new SpireField<String>(() -> "");
    }

    @SpirePatch(
        clz = WebButton.class,
        method = SpirePatch.CONSTRUCTOR,
        paramtypez = { String.class, float.class, float.class, float.class, float.class, float.class, Color.class, Texture.class },
        requiredModId = "VUPShionMod"
    )
    public static class ConstructorPatch
    {
        /**
         * Skip the SteamApp check, and save URL string.
        */
        @SpireInsertPatch(rloc = 10)
        public static SpireReturn<Void> SkipSteamCheck(WebButton __instance, String url, float x, float y, float width, float height, float scale, Color inactiveColor, Texture img)
        {
            NonFinalUrlField.newUrl.set(__instance, url);
            return SpireReturn.Return();
        }
    }

    @SpirePatch(clz = WebButton.class, method = "update", requiredModId = "VUPShionMod")
    public static class UpdatePatch
    {
        /**
         * Fix the URL since it's empty.
        */
        public static ExprEditor Instrument()
        {
            return new ExprEditor()
            {
                @Override
                public void edit(MethodCall m) throws CannotCompileException
                {
                    if (m.getClassName().equals(WebButton.class.getName()) && m.getMethodName().equals("openWebpage")) {
                        m.replace("{" +
                                "$1 = (String)" + NonFinalUrlField.class.getName() + ".newUrl.get(this);" +
                                "$proceed($$);" +
                                "}");
                    }
                }
            };
        }
    }

}
