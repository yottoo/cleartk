/*
 * Copyright (c) 2014, Regents of the University of Colorado 
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
package org.cleartk.util.ae;

import java.io.File;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.CasIOUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.ViewUriUtil;

import com.google.common.annotations.Beta;

/**
 * This is a simple annotator for writing out XMI files, used by the parsing examples.
 * 
 * <br>
 * Copyright (c) 2014, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */

@Beta
public class XmiWriter extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription(File outputDirectory)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(
        XmiWriter.class,
        XmiWriter.PARAM_OUTPUT_DIRECTORY,
        outputDirectory);
  }

  public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

  @ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY)
  protected File outputDirectory;

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      File xmiFile = getXmiFile(jCas);
      CasIOUtil.writeXmi(jCas, xmiFile);
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  protected File getXmiFile(JCas jCas) throws AnalysisEngineProcessException {
    return new File(this.outputDirectory, ViewUriUtil.getURI(jCas).getFragment() + ".xmi");
  }

}
