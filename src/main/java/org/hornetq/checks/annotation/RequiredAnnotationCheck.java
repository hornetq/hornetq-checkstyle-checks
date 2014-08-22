/*
 * Copyright 2005-2014 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.checks.annotation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * @author Clebert Suconic
 */


public class RequiredAnnotationCheck extends Check
{

   public RequiredAnnotationCheck() {
   }

   private String annotationName;
   private Set<String> requiredParameters = new HashSet<String>();


   public void setAnnotationName(String annotationName) {
      this.annotationName = annotationName;
   }

   public void setRequiredParameters(String[] requiredPropertiesParameter) {
      if (requiredPropertiesParameter != null) {
         for (String item : requiredPropertiesParameter) {
            this.requiredParameters.add(item);
         }
      }
   }

   /**
    * Helper method for subclasses to return the name of an annotation
    *
    * @param aAnnotation
    * @return
    */
   protected String getAnnotationName(DetailAST aAnnotation) {
      DetailAST directname = aAnnotation.findFirstToken(TokenTypes.IDENT);

      if (directname != null) {
         return directname.getText();
      } else {
         //This means that annotation is specified with the full package name
         return aAnnotation.findFirstToken(TokenTypes.DOT).getLastChild().getText();
      }
   }


   /**
    * Helper method for subclasses to return the name of their properties
    *
    * @param aAnnotation
    * @return
    */
   protected String[] getAnnotationParameters(DetailAST aAnnotation) {

      LinkedList<DetailAST> parameters = new LinkedList<DetailAST>();

      DetailAST retVal = null;
      for (DetailAST i = aAnnotation.getFirstChild(); i != null; i = i.getNextSibling()) {
         if (i.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
            parameters.add(i);
         }
      }

      String[] names = new String[parameters.size()];

      int count = 0;
      for (DetailAST itemIter : parameters) {
         names[count++] = itemIter.getFirstChild().getText();
      }
      return names;


   }

   @Override
   public int[] getDefaultTokens() {
      return new int[]{TokenTypes.ANNOTATION};
   }

   @Override
   public void visitToken(DetailAST aAST) {
      String annotationName = getAnnotationName(aAST);


      // we only check the annotations that we were asked to
      if (annotationName.equals(this.annotationName)) {

         String names[] = getAnnotationParameters(aAST);

         int found = 0;
         for (String p : names) {
            if (requiredParameters.contains(p)) {
               found++;
            }
         }

         if (found != requiredParameters.size()) {
            StringBuilder propertiesText = new StringBuilder();
            Iterator<String> iter = requiredParameters.iterator();
            while (iter.hasNext()) {
               String text = iter.next();
               propertiesText.append(text);
               if (iter.hasNext()) {
                  propertiesText.append(",");
               }
            }
            log(aAST, "Annotation @{0} missing one of these required parameters: ({1})", this.annotationName, propertiesText.toString());
         }

      }
   }


}
