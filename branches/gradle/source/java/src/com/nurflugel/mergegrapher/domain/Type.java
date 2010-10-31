package com.nurflugel.mergegrapher.domain;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Mar 6, 2010 Time: 7:27:42 PM To change this template use File | Settings | File Templates. */
public enum Type
{
  A('A'),
  D('D');

  private char typeName;

  public static Type findByValue(char typeChar)
  {
    Type[] types = values();

    for (Type type : types)
    {
      if (type.typeName == typeChar)
      {
        return type;
      }
    }

    return null;
  }

  Type(char c)
  {
    typeName = c;
  }
}
