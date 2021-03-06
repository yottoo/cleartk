/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.test.util.type.Token;
import org.junit.Assert;
import org.junit.Test;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.base.Function;

/**
 * 
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class AnnotationStatisticsTest extends DefaultTestBase {

  @Test
  public void testNoOutcomeFeature() throws Exception {
    this.tokenBuilder.buildTokens(this.jCas, "0 1 2 3 4 5 6 7 8 9 10 11");
    List<Token> tokens = new ArrayList<Token>(JCasUtil.select(this.jCas, Token.class));
    List<Token> referenceTokens = tokens.subList(0, 8);
    List<Token> predictedTokens = tokens.subList(6, 10);

    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    stats.add(referenceTokens, predictedTokens);
    Assert.assertEquals(0.5, stats.precision(), 1e-10);
    Assert.assertEquals(0.25, stats.recall(), 1e-10);
    Assert.assertEquals(2 * 0.5 * 0.25 / (0.5 + 0.25), stats.f1(), 1e-10);

    Assert.assertEquals(10, stats.confusions().getCount(null, null));
  }

  @Test
  public void testWithOutcomeFeature() throws Exception {

    JCas referenceView = this.jCas.createView("Reference");
    this.tokenBuilder.buildTokens(
        referenceView,
        "  1 2 3 4 5 6 7 8           ",
        "1 2 3 4 5 6 7 8",
        "A A B B B B C C");

    JCas predictedView = this.jCas.createView("Predicted");
    this.tokenBuilder.buildTokens(
        predictedView,
        "            6 7 8 9         ",
        "6 7 8 9",
        "B B C C");

    Collection<Token> referenceTokens = new ArrayList<Token>(JCasUtil.select(
        referenceView,
        Token.class));
    Collection<Token> predictedTokens = new ArrayList<Token>(JCasUtil.select(
        predictedView,
        Token.class));

    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    Function<Token, ?> getSpan = AnnotationStatistics.annotationToSpan();
    Function<Token, String> getPOS = AnnotationStatistics.annotationToFeatureValue("pos");
    stats.add(referenceTokens, predictedTokens, getSpan, getPOS);

    Assert.assertEquals(0.5, stats.precision(), 1e-10);
    Assert.assertEquals(0.25, stats.recall(), 1e-10);
    Assert.assertEquals(2 * 0.5 * 0.25 / (0.5 + 0.25), stats.f1(), 1e-10);

    Assert.assertEquals(1.0, stats.precision("A"), 1e-10);
    Assert.assertEquals(0.0, stats.recall("A"), 1e-10);
    Assert.assertEquals(0.0, stats.f1("A"), 1e-10);

    Assert.assertEquals(0.5, stats.precision("B"), 1e-10);
    Assert.assertEquals(0.25, stats.recall("B"), 1e-10);
    Assert.assertEquals(2 * 0.5 * 0.25 / (0.5 + 0.25), stats.f1("B"), 1e-10);

    Assert.assertEquals(0.5, stats.precision("C"), 1e-10);
    Assert.assertEquals(0.5, stats.recall("C"), 1e-10);
    Assert.assertEquals(0.5, stats.f1("C"), 1e-10);

    Assert.assertEquals(0, stats.confusions().getCount("A", "A"));
    Assert.assertEquals(0, stats.confusions().getCount("A", "B"));
    Assert.assertEquals(0, stats.confusions().getCount("A", "C"));
    Assert.assertEquals(2, stats.confusions().getCount("A", null));
    Assert.assertEquals(0, stats.confusions().getCount("B", "A"));
    Assert.assertEquals(1, stats.confusions().getCount("B", "B"));
    Assert.assertEquals(0, stats.confusions().getCount("B", "C"));
    Assert.assertEquals(3, stats.confusions().getCount("B", null));
    Assert.assertEquals(0, stats.confusions().getCount("C", "A"));
    Assert.assertEquals(1, stats.confusions().getCount("C", "B"));
    Assert.assertEquals(1, stats.confusions().getCount("C", "C"));
    Assert.assertEquals(0, stats.confusions().getCount("C", null));
    Assert.assertEquals(0, stats.confusions().getCount(null, "A"));
    Assert.assertEquals(0, stats.confusions().getCount(null, "B"));
    Assert.assertEquals(1, stats.confusions().getCount(null, "C"));
    Assert.assertEquals(0, stats.confusions().getCount(null, null));
  }

  @Test
  public void testWithAnnotationConversion() throws Exception {
    JCas referenceView = this.jCas.createView("Reference");
    this.tokenBuilder.buildTokens(referenceView, "1 2 3 4  ", "1 2 3 4", "A A B B");

    JCas predictedView = this.jCas.createView("Predicted");
    this.tokenBuilder.buildTokens(predictedView, "0 1 2 3", "0 1 2 3", "B A B B");

    Collection<Token> referenceTokens = new ArrayList<Token>(JCasUtil.select(
        referenceView,
        Token.class));
    Collection<Token> predictedTokens = new ArrayList<Token>(JCasUtil.select(
        predictedView,
        Token.class));

    // use the text of the annotation, rather than its span, to determine match
    Function<Token, String> tokenToCoveredText = new Function<Token, String>() {
      @Override
      public String apply(Token token) {
        return token.getCoveredText();
      }
    };
    Function<Token, String> tokenToPOS = AnnotationStatistics.<Token> annotationToFeatureValue("pos");
    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    stats.add(referenceTokens, predictedTokens, tokenToCoveredText, tokenToPOS);

    Assert.assertEquals(0.5, stats.precision(), 1e-10);
    Assert.assertEquals(0.5, stats.recall(), 1e-10);
    Assert.assertEquals(0.5, stats.f1(), 1e-10);

    Assert.assertEquals(1.0, stats.precision("A"), 1e-10);
    Assert.assertEquals(0.5, stats.recall("A"), 1e-10);
    Assert.assertEquals(2 * 1.0 * 0.5 / (1.0 + 0.5), stats.f1("A"), 1e-10);

    double oneThird = 1.0 / 3;
    Assert.assertEquals(oneThird, stats.precision("B"), 1e-10);
    Assert.assertEquals(0.5, stats.recall("B"), 1e-10);
    Assert.assertEquals(2 * oneThird * 0.5 / (oneThird + 0.5), stats.f1("B"), 1e-10);

    Assert.assertEquals(1, stats.confusions().getCount("A", "A"));
    Assert.assertEquals(1, stats.confusions().getCount("A", "B"));
    Assert.assertEquals(0, stats.confusions().getCount("A", null));
    Assert.assertEquals(0, stats.confusions().getCount("B", "A"));
    Assert.assertEquals(1, stats.confusions().getCount("B", "B"));
    Assert.assertEquals(1, stats.confusions().getCount("B", null));
    Assert.assertEquals(0, stats.confusions().getCount(null, "A"));
    Assert.assertEquals(1, stats.confusions().getCount(null, "B"));
    Assert.assertEquals(0, stats.confusions().getCount(null, null));
  }

  private void appendLabels(
      int n,
      String reference,
      String predicted,
      StringBuilder sbTokens,
      StringBuilder sbReference,
      StringBuilder sbPredicted) {

    for (int i = 0; i < n; i++) {
      sbTokens.append(String.format("%s%s%d ", reference, predicted, i));
      sbReference.append(String.format("%s ", reference));
      sbPredicted.append(String.format("%s ", predicted));
    }

  }

  @Test
  public void testRocStats() throws Exception {

    StringBuilder sbTokens = new StringBuilder();
    StringBuilder sbReferenceLabels = new StringBuilder();
    StringBuilder sbPredictedLabels = new StringBuilder();

    // Create a confusion matrix
    appendLabels(200, "A", "A", sbTokens, sbReferenceLabels, sbPredictedLabels);
    appendLabels(100, "A", "B", sbTokens, sbReferenceLabels, sbPredictedLabels);
    appendLabels(100, "A", "C", sbTokens, sbReferenceLabels, sbPredictedLabels);
    appendLabels(50, "B", "A", sbTokens, sbReferenceLabels, sbPredictedLabels);
    appendLabels(150, "B", "B", sbTokens, sbReferenceLabels, sbPredictedLabels);
    appendLabels(50, "B", "C", sbTokens, sbReferenceLabels, sbPredictedLabels);
    appendLabels(25, "C", "A", sbTokens, sbReferenceLabels, sbPredictedLabels);
    appendLabels(25, "C", "B", sbTokens, sbReferenceLabels, sbPredictedLabels);
    appendLabels(50, "C", "C", sbTokens, sbReferenceLabels, sbPredictedLabels);

    // Note using POS tags as a lazy way to get values
    JCas referenceView = this.jCas.createView("Reference");
    this.tokenBuilder.buildTokens(
        referenceView,
        sbTokens.toString(),
        sbTokens.toString(),
        sbReferenceLabels.toString());

    JCas predictedView = this.jCas.createView("Predicted");
    this.tokenBuilder.buildTokens(
        predictedView,
        sbTokens.toString(),
        sbTokens.toString(),
        sbPredictedLabels.toString());

    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();

    Collection<Token> referenceTokens = new ArrayList<Token>(JCasUtil.select(
        referenceView,
        Token.class));
    Collection<Token> predictedTokens = new ArrayList<Token>(JCasUtil.select(
        predictedView,
        Token.class));

    // use the text of the annotation, rather than its span, to determine match
    Function<Token, String> tokenToCoveredText = new Function<Token, String>() {
      @Override
      public String apply(Token token) {
        return token.getCoveredText();
      }
    };

    Function<Token, String> tokenToPOS = AnnotationStatistics.<Token> annotationToFeatureValue("pos");

    stats.add(referenceTokens, predictedTokens, tokenToCoveredText, tokenToPOS);
    Assert.assertEquals(stats.countTruePositives("A"), 200);
    Assert.assertEquals(stats.countTrueNegatives("A"), 200);
    Assert.assertEquals(stats.countFalsePositives("A"), 75);
    Assert.assertEquals(stats.countFalseNegatives("A"), 200);
    Assert.assertEquals(stats.countTruePositives("B"), 150);
    Assert.assertEquals(stats.countTrueNegatives("B"), 250);
    Assert.assertEquals(stats.countFalsePositives("B"), 125);
    Assert.assertEquals(stats.countFalseNegatives("B"), 100);
    Assert.assertEquals(stats.countTruePositives("C"), 50);
    Assert.assertEquals(stats.countTrueNegatives("C"), 350);
    Assert.assertEquals(stats.countFalsePositives("C"), 150);
    Assert.assertEquals(stats.countFalseNegatives("C"), 50);
  }
}
