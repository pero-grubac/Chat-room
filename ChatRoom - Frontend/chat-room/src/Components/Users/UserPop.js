import React, { useState, useEffect } from "react";
import { Radio, Form, Button, Modal, Checkbox } from "antd";
import "./Users.css";
import { useDispatch } from "react-redux";
import { logout } from "../Redux/slices/userSlice";
import { changeRole } from "../../Services/user.service";
import { handleChangeRole } from "../Error/ErrorMessage";
import {  useNavigate } from "react-router-dom";

const { Item } = Form;
const { confirm } = Modal;
const roleMap = {
  ADMIN: "ROLE_ADMIN",
  MODERATOR: "ROLE_MODERATOR",
  KORISNIK: "ROLE_KORISNIK",
};
const UserPop = ({ onClose, role, permissions, id, lblBtn }) => {
  const [showModeratorOptions, setShowModeratorOptions] = useState(false);
  const [selectedRole, setSelectedRole] = useState("");
  const [checkedValues, setCheckedValues] = useState([]);
   const dispatch = useDispatch();
   const navigate = useNavigate();

  useEffect(() => {
    setSelectedRole(role);
    if (role === "MODERATOR") {
      setShowModeratorOptions(true);
      setCheckedValues(permissions);
    } else {
      setShowModeratorOptions(false);
      setCheckedValues([]);
    }
  }, [role, permissions]);

  const handleRoleChange = (e) => {
    const { value } = e.target;
    setSelectedRole(value);
    if (value === "MODERATOR") {
      setShowModeratorOptions(true);
    } else {
      setShowModeratorOptions(false);
      setCheckedValues([]);
    }
  };

  const handleCheckboxChange = (checkedValues) => {
    setCheckedValues(checkedValues);
  };

  const handleClose = () => {
    onClose();
  };

  const isUserModified = () => {
    if (selectedRole !== role) {
      return true;
    }
    if (checkedValues.length !== permissions.length) {
      return true;
    }

    const sortedCheckedValues = [...checkedValues].sort();
    const sortedPermissions = [...permissions].sort();

    for (let i = 0; i < sortedCheckedValues.length; i++) {
      if (sortedCheckedValues[i] !== sortedPermissions[i]) {
        return true;
      }
    }
    return false;
  };
  const verifyData = (user) => {
    if (user.role === "ROLE_KORISNIK") user.permissions = [];
    if (user.role === "ROLE_ADMIN")
      user.permissions = ["ADD", "DELETE", "UPDATE"];
    setCheckedValues("ADD");
    setCheckedValues("ADDa");
  };

  const handleFunction = async () => {
    const user = {
      id: id,
      role: roleMap[selectedRole],
      permissions: checkedValues,
    };
    verifyData(user);
    confirm({
      title: "Are you sure you want to change this user?",
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        try {
          if (isUserModified()) {
            await changeRole(user);
          }
        } catch (error) {
          console.log(error);
          handleChangeRole(error);
          dispatch(logout());
          navigate("/login", { replace: true });
        }
      },
      onClose,
    });
  };
  return (
    <Modal
      title="User Permissions"
      visible={true}
      onCancel={handleClose}
      footer={null}
    >
      <Form>
        <Item label="Role">
          <Radio.Group onChange={handleRoleChange} value={selectedRole}>
            <Radio.Button value="KORISNIK">USER</Radio.Button>
            <Radio.Button value="MODERATOR">MODERATOR</Radio.Button>
            <Radio.Button value="ADMIN">ADMIN</Radio.Button>
          </Radio.Group>
        </Item>
        {showModeratorOptions && (
          <Item label="Permissions">
            <Checkbox.Group
              onChange={handleCheckboxChange}
              value={checkedValues}
            >
              <Checkbox value="ADD">ADD</Checkbox>
              <Checkbox value="DELETE">DELETE</Checkbox>
              <Checkbox value="UPDATE">UPDATE</Checkbox>
            </Checkbox.Group>
          </Item>
        )}
        <Item style={{ textAlign: "center" }}>
          <Button
            type="primary"
            onClick={handleFunction}
            style={{ marginRight: "8px", marginBottom: "8px" }}
          >
            {lblBtn}
          </Button>
          <br />
          <Button onClick={handleClose}>Close</Button>
        </Item>
      </Form>
    </Modal>
  );
};

export default UserPop;
