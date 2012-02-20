package com.nurflugel.ivygrapher;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Oct 3, 2009 Time: 6:06:34 PM To change this template use File | Settings | File Templates. */
public enum NodeOrder
{
  TOP_TO_BOTTOM("TB"),
  BOTTOM_TO_TOP("BT"),
  RIGHT_TO_LEFT("RL"),
  LEFT_TO_RIGHT("LR");

  private String order;

  // -------------------------- STATIC METHODS --------------------------
  public static NodeOrder find(String text)
  {
    NodeOrder[] nodeOrders = values();

    for (NodeOrder nodeOrder : nodeOrders)
    {
      if (nodeOrder.getOrder().equals(text))
      {
        return nodeOrder;
      }
    }

    return TOP_TO_BOTTOM;
  }

  NodeOrder(String order)
  {
    this.order = order;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getOrder()
  {
    return order;
  }
}
