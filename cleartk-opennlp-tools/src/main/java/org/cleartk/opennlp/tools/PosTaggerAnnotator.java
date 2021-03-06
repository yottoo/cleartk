/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.opennlp.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.IoUtil;
import org.cleartk.util.ParamUtil;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
@Beta
@TypeCapability(inputs = { "org.cleartk.token.type.Sentence", "org.cleartk.token.type.Token" })
public class PosTaggerAnnotator extends JCasAnnotator_ImplBase {

  // public static AnalysisEngineDescription getDescription() throws ResourceInitializationException
  // {
  // AnalysisEngineDescription aed = AnalysisEngineFactory.createEngineDescription(
  // opennlp.uima.postag.POSTagger.class,
  // SyntaxComponents.TYPE_SYSTEM_DESCRIPTION,
  // UimaUtil.TOKEN_TYPE_PARAMETER, Token.class.getName(),
  // UimaUtil.POS_FEATURE_PARAMETER, "pos",
  // UimaUtil.SENTENCE_TYPE_PARAMETER, Sentence.class.getName());
  //
  // this is wrong!
  // ExternalResourceDescription erd =
  // ExternalResourceFactory.createExternalResourceDescription("opennlp.uima.ModelName",
  // POSModel.class,"/models/en-pos-maxent.bin");
  //
  // try {
  // bindResource(aed, "opennlp.uima.ModelName", erd);
  // } catch (InvalidXMLException e) {
  // // TODO Auto-generated catch block
  // throw new ResourceInitializationException(e);
  // }
  // return aed;
  // }

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(
        PosTaggerAnnotator.class,
        PARAM_POSTAG_MODEL_FILE,
        ParamUtil.getParameterValue(PARAM_POSTAG_MODEL_FILE, "/models/en-pos-maxent.bin"));
  }

  public static final String PARAM_POSTAG_MODEL_FILE = "postagModelFile";

  @ConfigurationParameter(
      name = PARAM_POSTAG_MODEL_FILE,
      mandatory = true,
      description = "provides the path of the OpenNLP part-of-speech tagger model file, e.g.  resources/models/OpenNLP.POSTags.English.bin.gz.  See javadoc for opennlp.maxent.io.SuffixSensitiveGISModelReader.")
  private String postagModelFile;

  protected POSTagger posTagger;

  @Override
  public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
    super.initialize(uimaContext);
    try {
      InputStream modelInputStream = IoUtil.getInputStream(
          PosTaggerAnnotator.class,
          postagModelFile);
      POSModel posModel = new POSModel(modelInputStream);
      posTagger = new POSTaggerME(posModel);
    } catch (IOException ioe) {
      throw new ResourceInitializationException(ioe);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
      String[] tokenTexts = new String[tokens.size()];
      for (int i = 0; i < tokenTexts.length; ++i) {
        tokenTexts[i] = tokens.get(i).getCoveredText();
      }
      String[] tags = posTagger.tag(tokenTexts);
      for (int i = 0; i < tags.length; ++i) {
        tokens.get(i).setPos(tags[i]);
      }
    }
  }
}
