package com.yaetoti.localskinsystem.client.texture;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yaetoti.gif.blocks.*;
import com.yaetoti.gif.io.GifReader;
import com.yaetoti.gif.utils.GifColorTableType;
import com.yaetoti.gif.utils.GifLzwUtils;
import com.yaetoti.io.DataInputLE;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.AbstractTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

public class SampleTexture extends AbstractTexture {
  private RandomAccessFile m_file;
  private GifReader m_reader;

  private GifColorTable m_globalTable = null;
  private GifColorTable m_localTable = null;
  private GifLogicalScreenDescriptor m_descriptor = null;
  private GifImageDescriptor m_imageDescriptor = null;
  private GifGraphicsControlExtension m_extension = null;
  private GifTableBasedImageData m_imageData = null;

  private int m_width;
  private int m_height;
  private long m_pointer = 0;

  private float lastDelayMs = 0;

  public SampleTexture() throws IOException {
    //m_file = new RandomAccessFile(FabricLoader.getInstance().getGameDir().resolve("image.gif").toString(), "r");
    m_file = new RandomAccessFile(FabricLoader.getInstance().getGameDir().resolve("E:\\PremiereExport\\Miraculous-london-Full-HD.gif").toString(), "r");
    m_file = new RandomAccessFile(FabricLoader.getInstance().getGameDir().resolve("E:\\PremiereExport\\Shrek2.gif").toString(), "r");
    m_reader = new GifReader(new DataInputLE(m_file));

    try {
      HandleNextFrame();
      UpdateTexture();
    } catch (IOException e) {
      System.out.println("Failed to upload texture");
      e.printStackTrace();
    }

    // Upload texture
    if (!RenderSystem.isOnRenderThread()) {
      RenderSystem.recordRenderCall(() -> {
        TextureUtil.prepareImage(this.getGlId(), m_width, m_height);
        Upload();
      });
    } else {
      TextureUtil.prepareImage(this.getGlId(), m_width, m_height);
      Upload();
    }

    System.out.println("Generated");
  }

  private void HandleNextFrame() throws IOException {
    while (true) {
      var element = m_reader.ReadElement();
      var type = element.GetElementType();

      // Trailer = repeat
      if (type == GifElementType.TRAILER) {
        m_file.seek(0L);
        m_reader.Reset();
        break;
      }

      if (type == GifElementType.LOGICAL_SCREEN_DESCRIPTOR) {
        m_descriptor = element.As();
        m_width = m_descriptor.logicalScreenWidth;
        m_height = m_descriptor.logicalScreenHeight;
        continue;
      }

      if (type == GifElementType.COLOR_TABLE) {
        GifColorTable colorTable = element.As();
        if (colorTable.type == GifColorTableType.GLOBAL) {
          m_globalTable = colorTable;
        } else {
          m_localTable = colorTable;
        }

        continue;
      }

      if (type == GifElementType.GRAPHIC_CONTROL_EXTENSION) {
        m_extension = element.As();
        lastDelayMs = m_extension.delayTime * 10;
        continue;
      }

      if (type == GifElementType.IMAGE_DESCRIPTOR) {
        m_imageDescriptor = element.As();
        continue;
      }

      if (type == GifElementType.TABLE_BASED_IMAGE_DATA) {
        m_imageData = element.As();
        break;
      }
    }
  }

  private void UpdateTexture() throws IOException {
    // Check if image is valid
    Objects.requireNonNull(m_descriptor);
    Objects.requireNonNull(m_globalTable);
    Objects.requireNonNull(m_imageDescriptor);
    Objects.requireNonNull(m_imageData);
    Objects.requireNonNull(m_imageData.imageData);

    // If no texture - fill with transparent color
    if (m_pointer == 0) {
      m_pointer = MemoryUtil.nmemAlloc(4L * m_width * m_height);
      for (int index = 0; index < m_width * m_height; index++) {
        int colorIndex = m_descriptor.backgroundColorIndex;
        MemoryUtil.memPutByte(m_pointer + index * 4L, (byte) 0);
        MemoryUtil.memPutByte(m_pointer + index * 4L + 1, (byte) 0);
        MemoryUtil.memPutByte(m_pointer + index * 4L + 2, (byte) 0);
        MemoryUtil.memPutByte(m_pointer + index * 4L + 3, (byte) 0);
      }
    }

    // Set active color table
    GifColorTable activeTable = m_localTable == null ? m_globalTable : m_localTable;

    // Decode image data
    byte[] indices = GifLzwUtils.Decode(m_imageData.lzwMinimumCodeSize, m_imageData.imageData);
    Objects.requireNonNull(indices);
    if (indices.length != m_imageDescriptor.imageWidth * m_imageDescriptor.imageHeight) {
      throw new RuntimeException("Malformed color indices");
    }

    // Modify texture data
    int index = 0;
    for (int y = 0; y < m_imageDescriptor.imageHeight; y++) {
      for (int x = 0; x < m_imageDescriptor.imageWidth; x++) {
        int address = (y + m_imageDescriptor.imageTopPosition) * m_width + (x + m_imageDescriptor.imageLeftPosition);
        int colorIndex = indices[index] & 0xFF;

        // Skip transparent pixels
        if (m_extension != null && m_extension.transparentColorFlag && colorIndex == m_extension.transparentColorIndex) {
          ++index;
          continue;
        }

        // Set color
        MemoryUtil.memPutByte(m_pointer + address * 4L, activeTable.table[colorIndex * 3]);
        MemoryUtil.memPutByte(m_pointer + address * 4L + 1, activeTable.table[colorIndex * 3 + 1]);
        MemoryUtil.memPutByte(m_pointer + address * 4L + 2, activeTable.table[colorIndex * 3 + 2]);
        MemoryUtil.memPutByte(m_pointer + address * 4L + 3, (byte) 255);

        // Advance
        ++index;
      }
    }
  }

  public float GetLastDelayMs() {
    return lastDelayMs;
  }

  public void Upload() {
    try {
      HandleNextFrame();
      UpdateTexture();
      System.out.println("Updated");
    } catch (IOException e) {
      System.out.println("Failed to upload texture");
      e.printStackTrace();
    }

    bindTexture();

    GlStateManager._pixelStore(GlConst.GL_UNPACK_ROW_LENGTH, 0);
    GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_PIXELS, 0);
    GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_ROWS, 0);
    GlStateManager._pixelStore(GlConst.GL_UNPACK_ALIGNMENT, 4);
    GlStateManager._texSubImage2D(GlConst.GL_TEXTURE_2D, 0, 0, 0, m_width, m_height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, m_pointer);

    int error = GL11.glGetError();
    if (error != GL11.GL_NO_ERROR) {
      System.out.println("OpenGL Error: " + error);
    } else {
      System.out.println("OpenGL Success");
    }
  }

  @Override
  public int getGlId() {
    return super.getGlId();
  }

  @Override
  public void close() {
    try {
      m_file.close();
    } catch (IOException e) {
      System.out.println("Error closing file");
    }

    MemoryUtil.nmemFree(m_pointer);
    this.clearGlId();
  }
}
