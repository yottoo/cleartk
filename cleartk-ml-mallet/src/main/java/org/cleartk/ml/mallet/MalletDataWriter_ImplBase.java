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
package org.cleartk.ml.mallet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.encoder.features.BooleanEncoder;
import org.cleartk.ml.encoder.features.NameNumber;
import org.cleartk.ml.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.ml.encoder.features.NumberEncoder;
import org.cleartk.ml.encoder.features.StringEncoder;
import org.cleartk.ml.jar.DataWriter_ImplBase;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * This training data consumer produces training data suitable for <a
 * href="http://mallet.cs.umass.edu/index.php/SimpleTagger_example"> Mallet Conditional Random Field
 * (CRF) tagger</a>.
 * 
 * Each line of the training data contains a string representation of each feature followed by the
 * label/result for that instance.
 * 
 * @author Philip Ogren
 */
public abstract class MalletDataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE extends MalletClassifierBuilder_ImplBase<? extends MalletClassifier_ImplBase<OUTCOME_TYPE>, OUTCOME_TYPE>, OUTCOME_TYPE>
    extends DataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE, List<NameNumber>, OUTCOME_TYPE, String> {

  public MalletDataWriter_ImplBase(File outputDirectory) throws IOException {
    super(outputDirectory);
    NameNumberFeaturesEncoder fe = new NameNumberFeaturesEncoder();
    fe.addEncoder(new NumberEncoder());
    fe.addEncoder(new BooleanEncoder());
    fe.addEncoder(new StringEncoder());
    this.setFeaturesEncoder(fe);
  }

  @Override
  public void writeEncoded(List<NameNumber> features, String outcome)
      throws CleartkProcessingException {
    if (outcome == null) {
      throw CleartkProcessingException.noInstanceOutcome(features);
    }
    if (features.size() == 0) {
      trainingDataWriter.print("null:0 ");
    } else {
      for (NameNumber nameNumber : features) {
        trainingDataWriter.print(nameNumber.name + ":" + nameNumber.number + " ");
      }
    }
    this.trainingDataWriter.print(outcome);
    this.trainingDataWriter.println();
  }

}
