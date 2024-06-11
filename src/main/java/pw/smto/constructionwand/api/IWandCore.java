package pw.smto.constructionwand.api;

public interface IWandCore extends IWandUpgrade
{
    int getColor();

    IWandAction getWandAction();
}
