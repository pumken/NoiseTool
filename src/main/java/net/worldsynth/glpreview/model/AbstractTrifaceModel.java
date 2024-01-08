/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.worldsynth.glpreview.model;

import net.worldsynth.glpreview.PrimitiveType;
import net.worldsynth.glpreview.TriFace;


public abstract class AbstractTrifaceModel extends AbstractModel<TriFace> {

    @Override
    public final PrimitiveType getPrimitivesType() {
        return PrimitiveType.TRIFACES;
    }

    @Override
    protected void initVertexArray(int primitivesCount) {
        vertexPositionArray = new float[primitivesCount * 12];
        vertexColorArray = new float[primitivesCount * 12];
        vertexNormalArray = new float[primitivesCount * 12];
    }

    @Override
    protected void insertVertexArray(TriFace primitive, int index) {
        insertVertexPositionArray(vertexPositionArray, primitive, index);
        insertVertexColorArray(vertexColorArray, primitive, index);
        insertVertexNormalArray(vertexNormalArray, primitive, index);
    }

    private void insertVertexPositionArray(float[] vertexArray, TriFace face, int index) {
        //Iterate vertices
        for(int j = 0; j < 3; j++) {
            //Iterate vector components
            vertexPositionArray[index * 12 + j * 4] = face.vertices[j * 3];
            vertexPositionArray[index * 12 + j * 4 + 1] = face.vertices[j * 3 + 1];
            vertexPositionArray[index * 12 + j * 4 + 2] = face.vertices[j * 3 + 2];
            vertexPositionArray[index * 12 + j * 4 + 3] = 1.0f;
        }
    }

    private void insertVertexColorArray(float[] vertexArray, TriFace face, int index) {
        //Iterate vertices
        for(int j = 0; j < 3; j++) {
            //Iterate color components RGBA
            vertexColorArray[index * 12 + j * 4] = face.color[j * 3];
            vertexColorArray[index * 12 + j * 4 + 1] = face.color[j * 3 + 1];
            vertexColorArray[index * 12 + j * 4 + 2] = face.color[j * 3 + 2];
            vertexColorArray[index * 12 + j * 4 + 3] = 1.0f;
        }
    }

    private void insertVertexNormalArray(float[] vertexArray, TriFace face, int index) {
        //Iterate vertices
        for(int j = 0; j < 3; j++) {
            //Iterate vector components
            vertexNormalArray[index * 12 + j * 4] = face.normal[0];
            vertexNormalArray[index * 12 + j * 4 + 1] = face.normal[1];
            vertexNormalArray[index * 12 + j * 4 + 2] = face.normal[2];
            vertexNormalArray[index * 12 + j * 4 + 3] = 1.0f;
        }
    }
}