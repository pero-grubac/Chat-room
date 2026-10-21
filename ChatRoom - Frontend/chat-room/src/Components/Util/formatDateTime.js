// "21.10.2026 9:05" in the viewer's time zone
export const formatDateTime = (dateTimeString) => {
  const date = new Date(dateTimeString);
  const formattedDate = `${date.getDate()}.${date.getMonth() + 1}.${date.getFullYear()}`;
  const formattedTime = `${date.getHours()}:${date.getMinutes().toString().padStart(2, "0")}`;
  return `${formattedDate} ${formattedTime}`;
};
