export const parseTemplate = (template: string, variables: Record<string, string>) => {
  return template.replace(/{{(.*?)}}/g, (_, key) => {
    return variables[key.trim()] || ""
  })
}