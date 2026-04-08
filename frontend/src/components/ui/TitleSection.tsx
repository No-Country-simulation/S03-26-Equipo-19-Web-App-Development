type TitleSectionProps = {
  text: string,
   className?: string;
}

const TitleSection = ({text, className = ""}:TitleSectionProps) => {
  return (
     <p className={`text-xl md:text-3xl text-primary font-bold mb-2 ${className}`}>{text}</p>
  )
}

export default TitleSection