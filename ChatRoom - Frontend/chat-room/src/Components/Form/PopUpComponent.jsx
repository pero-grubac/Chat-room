import { Button, Form, Input } from "antd";
import "./PopUp.css";

// One-field form used to create or rename a room and to adjust a comment
const PopUpComponent = ({ onClose, roomName, handleSubmit, labelName, buttonText, maxLength }) => {
  return (
    <div className="new-forum-room-container">
      <div className="form-container">
        <Form
          name="basic"
          onFinish={handleSubmit}
          labelCol={{ span: 8 }}
          wrapperCol={{ span: 16 }}
          style={{
            width: "fit-content",
            padding: "20px",
            borderRadius: "8px",
            backgroundColor: "white",
            boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
          }}
          autoComplete="off"
        >
          <Form.Item
            label={labelName}
            name="name"
            initialValue={roomName}
            rules={[{ required: true, whitespace: true, message: `Please enter the ${labelName.toLowerCase()}` }]}
            wrapperCol={{ span: 24 }}
          >
            {/* The limit matches the database column, so the server never has to reject the length */}
            <Input.TextArea
              autoSize={{ minRows: 1, maxRows: 10 }}
              maxLength={maxLength}
              showCount
              style={{ width: "300px" }}
            />
          </Form.Item>
          <Form.Item wrapperCol={{ span: 24 }} className="form-actions">
            <Button type="primary" htmlType="submit" style={{ marginRight: "8px", marginBottom: "8px" }}>
              {buttonText}
            </Button>
            <Button type="default" onClick={onClose}>
              Cancel
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
};

export default PopUpComponent;
