import React, { useState, useEffect } from "react";
import { Radio, Form, Button, Modal, Checkbox } from "antd";
import "./Users.css";
import { logout } from "../Redux/slices/userSlice";
import { approveUser } from "../../Services/user.service";
import { handleChangeRole, handleGetForumRooms } from "../Error/ErrorMessage";
import { useNavigate } from "react-router-dom";
import { getForumRooms } from "../../Services/forumRooms.Service";
import { useDispatch } from "react-redux";

const { Item } = Form;
const { confirm } = Modal;
const roleMap = {
  ADMIN: "ROLE_ADMIN",
  MODERATOR: "ROLE_MODERATOR",
  KORISNIK: "ROLE_KORISNIK",
};

const AcceptUserPop = ({ onClose, idUser }) => {
  const [selectedRole, setSelectedRole] = useState("");
  const [checkedValues, setCheckedValues] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState("");
  const [showModeratorOptions, setShowModeratorOptions] = useState(false);
  const [rooms, setRooms] = useState([]);
  const navigate = useNavigate();
  const dispatch = useDispatch();

  useEffect(() => {
    const fetchForumRooms = async () => {
      try {
        const response = await getForumRooms();
        setRooms(response.data);
      } catch (error) {
        console.log(error);
        handleGetForumRooms(error);
        dispatch(logout());
        navigate("/login", { replace: true });
      }
    };
    //   dispatch(authState());
    fetchForumRooms();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  const handleClose = () => {
    onClose();
  };
  const verifyData = () => {
    if (selectedRole === "ROLE_KORISNIK") setCheckedValues([]);
    if (selectedRole === "ROLE_ADMIN")
      setCheckedValues(["ADD", "DELETE", "UPDATE"]);
    if (selectedRole === "ROLE_MODERATOR" && checkedValues.length === 0)
      return false;
    if (rooms.length === 0) return false;
    return true;
  };
  function isRoomExists() {
    for (const room of rooms) {
      if (room.id === selectedRoom) {
        return true;
      }
    }
    return false;
  }
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
  const handleChangedRoom = (e) => {
    const { value } = e.target;
    setSelectedRoom(value);
  };
  const handleFunction = async () => {
    const user = {
      id: idUser,
      isApproved: true,
      role: roleMap[selectedRole],
      permissions: checkedValues,
      idRoom: selectedRoom,
    };
    console.log(checkedValues);
    confirm({
      title: "Are you sure you want to change this user?",
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        try {
          if (verifyData() && isRoomExists()) {
            //  console.log(user);
            await approveUser(user);
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
        <Item label="Rooms" required>
          <Radio.Group
            onChange={handleChangedRoom}
            value={selectedRoom}
            required
          >
            {rooms.map((room) => (
              <Radio.Button value={room.id}>{room.name}</Radio.Button>
            ))}
          </Radio.Group>
        </Item>
        <Item label="Role" required>
          <Radio.Group
            onChange={handleRoleChange}
            value={selectedRole}
            required
          >
            <Radio.Button value="KORISNIK">USER</Radio.Button>
            <Radio.Button value="MODERATOR">MODERATOR</Radio.Button>
            <Radio.Button value="ADMIN">ADMIN</Radio.Button>
          </Radio.Group>
        </Item>
        {showModeratorOptions && (
          <Item label="Permissions" required>
            <Checkbox.Group
              onChange={handleCheckboxChange}
              value={checkedValues}
              required
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
            Accept
          </Button>
          <br />
          <Button onClick={handleClose}>Close</Button>
        </Item>
      </Form>
    </Modal>
  );
};
export default AcceptUserPop;
