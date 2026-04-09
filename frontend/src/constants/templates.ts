export const templates = [
  {
    id: "1",
    name: "Follow-up",
    content: "Hi {{name}}, I wanted to follow up on our recent conversation. Do you have any questions I can help with?",
    variables: ["name"],
  },
  {
    id: "2",
    name: "Meeting Request",
    content: "Hi {{name}}, would you be available for a quick call this week to discuss {{topic}}?",
    variables: ["name", "topic"],
  },
  {
    id: "3",
    name: "Thank You",
    content: "Hi {{name}}, thank you for your time today! I'll send over the {{document}} as discussed.",
    variables: ["name", "document"],
  },
  {
    id: "4",
    name: "Proposal",
    content: "Dear {{name}},\n\nPlease find attached our proposal for {{company}}. We're excited about the opportunity to work together.\n\nBest regards",
    variables: ["name", "company"],
  },
  {
    id: "5",
    name: "Check-in",
    content: "Hi {{name}}, just checking in to see how things are going with {{product}}. Let me know if you need any assistance!",
    variables: ["name", "product"],
  },
]