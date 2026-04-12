package com.example.androidpractice08_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.androidpractice08_1.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSax, btnDom, btnPull, btnWrite;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSax = findViewById(R.id.btn_sax);
        btnDom = findViewById(R.id.btn_dom);
        btnPull = findViewById(R.id.btn_pull);
        btnWrite = findViewById(R.id.btn_write);
        tvResult = findViewById(R.id.tv_result);

        btnSax.setOnClickListener(this);
        btnDom.setOnClickListener(this);
        btnPull.setOnClickListener(this);
        btnWrite.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sax) {
            parseWithSAX();
        } else if (v.getId() == R.id.btn_dom) {
            parseWithDOM();
        } else if (v.getId() == R.id.btn_pull) {
            parseWithPull();
        } else if (v.getId() == R.id.btn_write) {
            generateXML();
        }
    }

    // ==========================================
    // 1. SAX 解析
    // 特点：基于事件驱动，只读不存，内存占用极低，适合大文件
    // ==========================================
    private void parseWithSAX() {
        try {
            // 读取 raw 文件夹下的 data.xml
            InputStream is = getResources().openRawResource(R.raw.data);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader reader = factory.newSAXParser().getXMLReader();
            ContentHandler handler = new ContentHandler();
            reader.setContentHandler(handler);

            reader.parse(new InputSource(is));

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("SAX解析出错: " + e.getMessage());
        }
    }

    // SAX 的核心：回调处理器
    class ContentHandler extends DefaultHandler {
        private final List<String> names = new ArrayList<>();
        private final List<String> ages = new ArrayList<>();
        private String currentTag;
        private String currentValue;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            currentTag = localName; // 记录当前标签名
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentValue = new String(ch, start, length); // 获取标签内容
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            // 标签结束时，根据标签名将数据存入集合
            if ("name".equals(localName)) {
                names.add(currentValue);
            } else if ("age".equals(localName)) {
                ages.add(currentValue);
            }
        }

        @Override
        public void endDocument() {
            // 解析完成后更新 UI
            runOnUiThread(() -> {
                StringBuilder sb = new StringBuilder("SAX 解析结果：\n");
                for (int i = 0; i < names.size(); i++) {
                    sb.append("姓名：").append(names.get(i))
                            .append("，年龄：").append(ages.get(i)).append("\n\n");
                }
                tvResult.setText(sb.toString());
            });
        }
    }

    // ==========================================
    // 2. DOM 解析
    // 特点：将整个 XML 加载到内存形成树结构，操作灵活但极耗内存，不适合大文件
    // ==========================================
    private void parseWithDOM() {
        try {
            InputStream is = getResources().openRawResource(R.raw.data);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is); // 一次性加载整个文档

            Element root = doc.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("person");

            StringBuilder sb = new StringBuilder("DOM 解析结果：\n");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    // DOM 可以通过标签名直接获取节点
                    String name = element.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                    String age = element.getElementsByTagName("age").item(0).getChildNodes().item(0).getNodeValue();
                    sb.append("姓名：").append(name).append("，年龄：").append(age).append("\n\n");
                }
            }
            tvResult.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("DOM解析出错: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. PULL 解析 (Android 推荐)
    // 特点：主动拉取，代码逻辑清晰，内存占用低，Android 系统底层默认使用
    // ==========================================
    private void parseWithPull() {
        try {
            InputStream is = getResources().openRawResource(R.raw.data);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, "UTF-8");

            StringBuilder sb = new StringBuilder("PULL 解析结果：\n");
            String name = "";
            String age = "";

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        // 遇到开始标签
                        if ("name".equals(tagName)) {
                            name = parser.nextText(); // 直接读取下一个文本节点
                        } else if ("age".equals(tagName)) {
                            age = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        // 遇到结束标签，如果是 person 结束，说明一条数据读完了
                        if ("person".equals(tagName)) {
                            sb.append("姓名：").append(name).append("，年龄：").append(age).append("\n\n");
                        }
                        break;
                }
                eventType = parser.next(); // 手动移动到下一个事件
            }
            tvResult.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("PULL解析出错: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. 生成 XML
    // ==========================================
    private void generateXML() {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlSerializer serializer = factory.newSerializer();
            StringWriter writer = new StringWriter();
            serializer.setOutput(writer);

            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, "persons");

            serializer.startTag(null, "person");
            serializer.attribute(null, "id", "1");

            serializer.startTag(null, "name");
            serializer.text("GeneratedUser");
            serializer.endTag(null, "name");

            serializer.startTag(null, "age");
            serializer.text("25");
            serializer.endTag(null, "age");

            serializer.endTag(null, "person");
            serializer.endTag(null, "persons");
            serializer.endDocument();

            tvResult.setText("生成的XML如下：\n" + writer.toString());

        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("生成XML出错");
        }
    }
}